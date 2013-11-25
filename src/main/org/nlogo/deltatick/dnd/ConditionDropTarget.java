package org.nlogo.deltatick.dnd;

import org.nlogo.api.Patch;
import org.nlogo.deltatick.*;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;

public class ConditionDropTarget
        extends DropTarget {

    ConditionBlock cBlock;

    public ConditionDropTarget(ConditionBlock cBlock) {
        super(cBlock);
        this.cBlock = cBlock;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (((ConditionBlock) block).canAddBlock()) {
                if (o instanceof BehaviorBlock) {

                    if (((ConditionBlock) block).addedRectPanel) {
                        ((ConditionBlock) block).hideRectPanel();
                        ((ConditionBlock) block).addedRectPanel = false;
                    }

                    addCodeBlock((BehaviorBlock) o);
                    // Set this behavior's parent BreedBlock
                    ((BehaviorBlock) o).setMyBreedBlock(((BreedBlock) block.getMyBreedBlock()));

                    if (((BehaviorBlock) o).getIsMutate() == true) {
                        ((BreedBlock) (block).getMyBreedBlock()).setReproduceUsed(true);
                    }
                    // If breed has traits, and any trait is applicable to this behavior block then show a the panel
                    if (((BreedBlock) block.getMyBreedBlock()).numTraits() > 0) {
                        boolean addPanel = false;
                        for (String traitName : ((BehaviorBlock) o).getApplicableTraits()) {
                            if (((BreedBlock) block.getMyBreedBlock()).hasTrait(traitName)) {
                                addPanel = true;
                            }
                        }
                        if (addPanel) {
                            ((BehaviorBlock) o).removeBehaviorInput();
                            ((BehaviorBlock) o).addTraitblockPanel();
                        }
                    }

                    new BehaviorDropTarget((BehaviorBlock) o);
                    return true;
                }
                else if (o instanceof ConditionBlock) {

                    if (((ConditionBlock) block).addedRectPanel) {
                        ((ConditionBlock) block).hideRectPanel();
                        ((ConditionBlock) block).addedRectPanel = false;
                    }

                    addCodeBlock((ConditionBlock) o);
                    new ConditionDropTarget((ConditionBlock) o);
                    return true;
                }
                else if (o instanceof PatchBlock) {
                    addCodeBlock((PatchBlock) o);
                    //new ConditionDropTarget((PatchBlock) o);
                    return true;
                }
                else if (o instanceof TraitBlockNew) {
                    addCodeBlock((TraitBlockNew) o);
                    cBlock.setTrait(((TraitBlockNew) o).getTraitName());
                    cBlock.removeBehaviorInput();
                    ((TraitBlockNew) o).hideRemoveButton();

                    return true;
                }
            }
            else {
                // Display error message
                String message = new String("Oops! You can only add one block here.");
                JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            //return false; - commented out by A. (nov 27)
        }
        return false;
    }
    public void dragEnter(DropTargetDragEvent dtde) {
        if (((ConditionBlock) block).canAddBlock()) {
            if ((dtde.isDataFlavorSupported(CodeBlock.behaviorBlockFlavor)) ||
                    (dtde.isDataFlavorSupported(CodeBlock.conditionBlockFlavor))) {
                ((ConditionBlock) block).showRectPanel();
                ((ConditionBlock) block).addedRectPanel = true;
            }
        }
    }

    public void dragExit(DropTargetEvent dte) {
        if (((ConditionBlock) block).canAddBlock()) {
            if (((ConditionBlock) block).addedRectPanel) {
                ((ConditionBlock) block).hideRectPanel();
                ((ConditionBlock) block).addedRectPanel = false;
            }
        }
    }

}
