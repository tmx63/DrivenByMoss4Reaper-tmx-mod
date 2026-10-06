// Copyright (c) 2018-2026 by Jürgen Moßgraber (www.mossgrabers.de)
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

#include <cstring>
#include <sstream>

#include "WrapperGSL.h"
#include "ActionProcessor.h"
#include "ReaperUtils.h"
#include "Collectors.h"

// [tmx mod] Set to false to silence the action-state log in the REAPER console
static const bool ACTION_STATE_LOG = true;


/**
 * Constructor.
 *
 * @param aModel The model to share data
 */
ActionProcessor::ActionProcessor(Model& aModel) : OscProcessor(aModel), selectionIsActive(false), selectedAction(0)
{
	// Intentionally empty
}


/** {@inheritDoc} */
void ActionProcessor::Process(std::deque<std::string>& path) noexcept
{
	const char* part = SafeGet(path, 0);
	if (std::strcmp(part, "select") == 0)
	{
		// Bring Reaper window to front
		HWND mainWindow = GetMainHwnd();
		SetForegroundWindow(mainWindow);

		// Open the action selection dialog
		PromptForAction(1, 0, 0);

		// If variable is true, action selection will be checked from the Run() loop
		this->selectionIsActive = true;
		this->selectedAction = 0;
		return;
	}
}


/** {@inheritDoc} */
void ActionProcessor::Process(std::deque<std::string>& path, int value) noexcept
{
	Main_OnCommandEx(value, 0, ReaperUtils::GetProject());
}


/** {@inheritDoc} */
void ActionProcessor::Process(std::deque<std::string>& path, const std::string& value) noexcept
{
	// [tmx mod] Java asks to report the toggle state of an action (e.g. for button LEDs)
	if (std::strcmp(SafeGet(path, 0), "watch") == 0)
	{
		if (value.empty() || value.find('/') != std::string::npos)
			return;
		// (Re-)registering forces the current state to be sent again
		if (this->watchedActions.size() < 256 || this->watchedActions.count(value) > 0)
			this->watchedActions[value] = -2;
		if (ACTION_STATE_LOG)
		{
			int id = std::atoi(value.c_str());
			if (id <= 0)
				id = NamedCommandLookup(value.c_str());
			std::ostringstream msg;
			msg << "[DBM action] watch request: " << value << " (command ID " << id << ")\n";
			ShowConsoleMsg(msg.str().c_str());
		}
		return;
	}

	int id = std::atoi(value.c_str());
	if (id <= 0)
		id = NamedCommandLookup(value.c_str());
	Main_OnCommandEx(id, 0, ReaperUtils::GetProject());
}


/**
 * Collect the action selection.
 *
 * @param ss The stream where to append the formatted data
 */
void ActionProcessor::CollectData(std::ostringstream& ss)
{
	// [tmx mod] Report changed toggle states of watched actions as /action/state/{id} {0|1|-1}
	for (auto& entry : this->watchedActions)
	{
		int id = std::atoi(entry.first.c_str());
		if (id <= 0)
			id = NamedCommandLookup(entry.first.c_str());
		const int state = id > 0 ? GetToggleCommandStateEx(0, id) : -1;
		if (ACTION_STATE_LOG && state != entry.second)
		{
			std::ostringstream msg;
			msg << "[DBM action] state sent: " << entry.first << " = " << state << "\n";
			ShowConsoleMsg(msg.str().c_str());
		}
		entry.second = Collectors::CollectIntValue(ss, "/action/state/" + entry.first, entry.second, state, false);
	}

	if (this->selectedAction <= 0)
		return;
	const char* cmd = ReverseNamedCommandLookup(this->selectedAction);
	if (cmd != nullptr)
		Collectors::CollectStringValue(ss, "/action/select", "", "_" + std::string(cmd), true);
	else
		Collectors::CollectIntValue(ss, "/action/select", -1, this->selectedAction, true);
	this->selectedAction = -1;
}


/**
 * Check if the user selected an action from the action dialog and/or closed the dialog.
 */
void ActionProcessor::CheckActionSelection() noexcept
{
	// Only if action selection is active
	if (!this->selectionIsActive)
		return;

	// Check if an action is selected
	const int action = PromptForAction(0, 0, 0);
	if (action == -1)
	{
		// User wants to close the dialog and finish the selection process
		PromptForAction(-1, 0, 0);
		this->selectionIsActive = false;
	}
	else if (action > 0)
	{
		// An action was selected
		this->selectedAction = action;
	}
}
