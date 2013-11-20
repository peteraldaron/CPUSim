package cpusim.util;

import cpusim.Machine;
import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.desktop.DesktopController;
import cpusim.microinstruction.IO;
import cpusim.module.ControlUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Dialogs;

public class UpdateDisplayManager
        implements ChangeListener<Machine.StateWrapper>, CPUSimConstants{

    private DesktopController desktop;
    private Mediator mediator;
   
    public UpdateDisplayManager(Mediator mediator, DesktopController desktop) {
        this.desktop = desktop;
        this.mediator = mediator;
        mediator.getMachine().stateProperty().addListener(this);
    }

    /**
     * Receive notifications that a module
     * has modified a property. This method is called within the
     * run thread and so any GUI events it generates need to be called
     * using invokeLater
     * @param stateWrapper the variable that is being listened
     * @param oldStateWrapper the value of the state before changed
     * @param newStateWrapper the new state object
     */
    public void changed(ObservableValue<? extends Machine.StateWrapper> stateWrapper,
                        Machine.StateWrapper oldStateWrapper, Machine.StateWrapper newStateWrapper) {

        //The cases are
        //identical except for the error message that is displayed.
        if (newStateWrapper.getState() == Machine.State.START_OF_EXECUTE_THREAD) {
            Platform.runLater(new Runnable() {
                public void run() {
                    desktop.setInRunningMode(true);
                }
            });
            if (desktop.getInDebugMode()) {
                //The Modify menu is already disabled in Debug mode,
                // as are the run menu items in the Execute menu.
                //So just disable the buttons on the tool bar
                Platform.runLater(new Runnable() {
                    public void run() {
                        desktop.getDebugToolBarController().setDisableAllButtons(true);
                    }
                });
            }
        }
        else if (newStateWrapper.getState() == Machine.State.EXCEPTION_THROWN ||
                newStateWrapper.getState() == Machine.State.EXECUTION_HALTED ||
                newStateWrapper.getState() == Machine.State.EXECUTION_ABORTED) {
            Platform.runLater(new Runnable() {
                public void run() {
                    updateDesktop(false);
                    desktop.setInRunningMode(false);
                    if (desktop.getInDebugMode()) {
                        desktop.getDebugToolBarController().setDisableAllButtons(false);
                    }
                }
            });
        }
        else if (newStateWrapper.getState() == Machine.State.HALTED_STEP_BY_MICRO) {
            Platform.runLater(new Runnable() {
                public void run() {
                    updateDesktop(true);
                    desktop.setInRunningMode(false);
                    if (desktop.getInDebugMode()) {
                        desktop.getDebugToolBarController().setDisableAllButtons(false);
                    }
                }
            });
        }
        //ignore all other property change events, such as start of
        // machine cycle and start of microinstruction.
    }

    /**
     * updates the display setting of elements in desktop
     * @param outlineChanges state if the outline has changed
     */
    private void updateDesktop(boolean outlineChanges) {
        if (desktop.getInDebugMode()) {
            desktop.getDebugToolBarController().updateDisplay(false, outlineChanges);
        }
        else {
            mediator.getMachine().getControlUnit().reset();
            mediator.getMachine().resetAllChannelsButConsole();
        }
    }
}
