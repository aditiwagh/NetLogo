package org.nlogo.app;

import org.nlogo.agent.Observer;
import org.nlogo.api.SimpleJobOwner;
import org.nlogo.api.CompilerException;
import org.nlogo.deltatick.*;
//import org.nlogo.deltatick.PopupMenu;
import org.nlogo.deltatick.dialogs.*;
import org.nlogo.deltatick.dnd.*;
import org.nlogo.deltatick.xml.*;
import org.nlogo.widget.NoteWidget;
import org.nlogo.window.*;
import org.nlogo.deltatick.SpeciesEditorPanel;


import org.nlogo.plot.PlotPen;
import org.nlogo.plot.PlotManager;

// java.awt contains all of the classes for creating user interfaces and for painting graphics and images -A. (sept 8)
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: Feb 17, 2010
 * Time: 10:24:15 PM
 * To change this template use File | Settings | File Templates.
 */
//TODO set the window to a larger size -A.
public class DeltaTickTab
	extends javax.swing.JPanel
    implements Events.SwitchedTabsEvent.Handler {

    //final means cannot be overriden and can only be initialized once -A. (sept 8)
    //toolBar is an object of class org.nlogo.swing.Toolbar -A. (sept8)
    final org.nlogo.swing.ToolBar toolBar;


    // breedTypeSelector is an object, BreedTypeSelector is a class -A. (sept 8)
    //an object is an instantiation of a class -A. (sept 8)
    BreedTypeSelector breedTypeSelector;
    TraitSelectorOld traitSelector;
    TraitTypeSelectorOld traitTypeSelector;
    VariationSelector variationSelector;
    EnvtTypeSelector envtTypeSelector;
    OperatorBlockBuilder obBuilder;
    UserInput userInput = new UserInput();

    JSeparator separator = new JSeparator();
    JPanel contentPanel = new JPanel();
    JPanel libraryPanel;
    BuildPanel buildPanel;
    SpeciesInspectorPanel speciesInspectorPanel;
    HashMap<BreedBlock, SpeciesInspectorPanel> speciesInspectorPanelMap = new HashMap<BreedBlock, SpeciesInspectorPanel>();

    SpeciesEditorPanel speciesEditorPanel;
    HashMap<String, SpeciesEditorPanel> speciesEditorPanelHashMap = new HashMap<String, SpeciesEditorPanel>();

    JButton loadLibrary;
    JButton addBreed;
    JButton addDiveIn;

    JButton speciesTry;

    JButton saveModelButton;
    JButton openModelButton;
    JButton addClear;

    JButton addTrackSpecies;
    JPopupMenu trackSpeciesPopUp;
    JMenuItem addHisto;
    JMenuItem addPlot;
    JMenuItem addMonitor;

    JButton buildBlock;
    JButton Not;

    boolean checkForErrors = true; //!< Flag that indicates if error checking needs to be done
    boolean plotsAlive = false;

    int count;   // to make sure tabbedpane doesn't get created more than once (Feb 23, 2012)
    int interfaceCount; // to make sure setup and go button doesn't get created more than once (May 13, 2012)
    int interfacePlotCount;
    int interfaceHistoCount; // to make sure extra histos are not added (Jan 15, 2013)
    int interfaceGraphCount;
    int interfaceSliderCount = 0; //to make sure mutation note is created just once (March 29, 2013)

    HashMap<String, WidgetWrapper> plotWrappers = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> mutationSliderWidgets = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> carryingCapacitySliderWidgets = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> noteWidgets = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> chooserWidgets = new HashMap<String, WidgetWrapper>();
    HashMap<String, WidgetWrapper> monitorWidgets = new HashMap<String, WidgetWrapper>();

    private final Double MUTATION_SLIDER_DEFAULT_VALUE = 0.0;
    private final Double CARRYING_CAPACITY_SLIDER_MIN_VALUE = 1.0;
    private final Double CARRYING_CAPACITY_SLIDER_MAX_VALUE = 100.0;
    private final Double CARRYING_CAPACITY_SLIDER_DEFAULT_VALUE = 50.0;

    // HashMaps to store slider values
    HashMap<String, Double> mutationSliderValues = new HashMap<String, Double>();
    HashMap<String, Double> carryingCapacitySliderValues = new HashMap<String, Double>();

    //InterfaceTab it;
    ProceduresTab pt;
    GUIWorkspace workspace;
    InterfacePanel interfacePanel;

    DeltaTickTab deltaTickTab = this;
    PlotManager plotManager;
    PlotNameFieldListener plotNameFieldListener = new PlotNameFieldListener();

    LibraryHolder libraryHolder;
    LibraryReader libraryReader;
    HashSet<String> openLibraries = new HashSet<String>();

    DeltaTickModelReader deltaTickModelParser;

    public final SimpleJobOwner defaultOwner ;

    //constructor -A. (sept 8)
    public DeltaTickTab( GUIWorkspace workspace , ProceduresTab pt, InterfacePanel interfacePanel) {
        this.workspace = workspace;
        this.interfacePanel = interfacePanel;
        this.pt = pt;


        this.plotManager = workspace.plotManager();

        this.breedTypeSelector = new BreedTypeSelector(workspace.getFrame());
        //this.it = it;
        this.traitSelector = new TraitSelectorOld( workspace.getFrame() );
        this.traitTypeSelector = new TraitTypeSelectorOld(workspace.getFrame());
        this.variationSelector = new VariationSelector(workspace.getFrame());
        this.envtTypeSelector = new EnvtTypeSelector(workspace.getFrame());
        this.obBuilder = new OperatorBlockBuilder(workspace.getFrame());
        obBuilder.setMyParent(this);

        defaultOwner =
          new SimpleJobOwner("DeltaTick Runner", workspace.world.mainRNG,
                             Observer.class);
        //creates new ToolBar - method declared below -A. (sept 8)
        toolBar = getToolBar();

        setLayout( new java.awt.BorderLayout() ) ;
		add( toolBar , java.awt.BorderLayout.NORTH ) ;
        add( contentPanel, java.awt.BorderLayout.CENTER );

        // Instantiate the DeltaTickModelParser
        deltaTickModelParser = new DeltaTickModelReader(workspace.getFrame(), this);

        //actually instantiates the object, declaration above does not instantiate until the constructor is executed
        //-A. (sept 8)

        // Create the library reader. There should only be one library reader that opens/reads library files
        libraryReader = new LibraryReader(workspace.getFrame(), deltaTickTab);

        libraryPanel = new JPanel();
        //libraryPanel.setLayout(new GridLayout(10,1));        // (int rows, int columns)
        libraryPanel.setLayout( new BoxLayout (libraryPanel, BoxLayout.Y_AXIS));

        //second line is making the entire buildPanel ready for stuff to be dragged -A. (sept 8)
        buildPanel = new BuildPanel( workspace );
        new BuildPanelDragSource(buildPanel);

        separator.setOrientation(SwingConstants.VERTICAL);

        java.awt.GridBagConstraints gridBagConstraints;

        contentPanel.setLayout(new java.awt.GridBagLayout());   // one row, columns expand

        //librarySeparator.setOrientation(SwingConstants.HORIZONTAL);
        // Fill: used when the component's display area is larger than the
        // component's requested size. It determines whether to resize the component,
        // and if so, how.  -A. (sept 8)
        //# HORIZONTAL: Make component wide enough to fill its display area horizontally, not change height
        // VERTICAL: '' tall enough to fill its display area vertically, not change its width.
        // BOTH: Make the component fill its display area entirely.

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1;
        contentPanel.add(buildPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.weighty = 1.0;
        contentPanel.add(separator, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        contentPanel.add(libraryPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;

        buildPanel.addRect("Click on Load blocks to get started!");

        //contentPanel.pack();

        count = 0;
        interfaceCount = 0;
        interfacePlotCount = 0;
        interfaceHistoCount = 0;
        interfaceGraphCount = 0; //to combine plots and histos into one
    }


    public void addCondition( ConditionBlock cBlock ) {
        new ConditionDropTarget(cBlock);
        //new PlantedCodeBlockDragSource(cBlock);
    }

    public void addDragSource( CodeBlock block ) {
        if (block instanceof TraitBlock) {
        }
        new CodeBlockDragSource( block );
    }


    public void addTrait( TraitBlock tBlock ) {
        new TraitDropTarget(tBlock);
        //tBlock.numberAgents();
    }

    public void addOperator ( OperatorBlock oBlock ) {
        new OperatorDropTarget (oBlock);
    }

    private final javax.swing.Action loadAction =
            new AbstractAction("Load Blocks") {
                JFileChooser fileChooser = new JFileChooser();

                public void actionPerformed (ActionEvent e) {
                    fileChooser.addChoosableFileFilter(new XMLFilter());
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    int returnVal = fileChooser.showOpenDialog(DeltaTickTab.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String fileName = new String(fileChooser.getSelectedFile().getAbsolutePath());
                        openLibrary(fileName);
                    }
                }
		/*new javax.swing.AbstractAction( "Load Behavior Library" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                openLibrary(null);
            }*/
        };

    public void openLibrary(String  fileName) {
        String currentLibraryName = new String();
        String thisLibraryName = new String();
        boolean libraryOpenSuccessful = false;

        // Aditi: The library reader reference MUST be saved (Apr 16, 2013)
        // this.libraryReader = new LibraryReader( workspace.getFrame() , deltaTickTab, fileName );
        thisLibraryName = libraryReader.readLibraryName(new File(fileName));
        if (openLibraries.contains(thisLibraryName)) {
            // Already open. Display error message.
            String message = new String("Oops! This library is already open!");
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        currentLibraryName = thisLibraryName;

        if ( count == 0 ) {
          	libraryHolder = new LibraryHolder();
     		libraryHolder.makeNewTab();

            libraryReader.openLibrary(fileName);

        	// if(buildPanel.getBgInfo().getLibrary() != null){
        		libraryPanel.add(libraryHolder);
        		libraryHolder.setTabName(currentLibraryName);

        		addBreed.setEnabled(true);
                addDiveIn.setEnabled(true); //ToDO: Set this true only when XML says so (Aditi, Sept 20, 2013)
                addTrackSpecies.setEnabled(true);
        		addClear.setEnabled(true);
                saveModelButton.setEnabled(true);
        	    buildPanel.removeRect();

        	    if (buildPanel.getMyBreeds().size() == 0) {
                    buildPanel.addRect("Click Add species to start building your model!");
                    buildPanel.repaint();
                    buildPanel.validate();
                }

        		deltaTickTab.contentPanel.validate();
        		count ++;

        }
         else if (count > 0 ) {

            libraryHolder.makeNewTab();
            libraryHolder.setTabName( currentLibraryName );

            // Aditi: Again, library reader MUST be saved (Apr 16, 2013)
            //this.libraryReader = new LibraryReader( workspace.getFrame(), deltaTickTab, fileName);
            libraryReader.openLibrary(fileName);
            deltaTickTab.contentPanel.validate();
         }

        libraryPanel.revalidate();
        libraryHolder.revalidate();
        // Add library name to openLibraries
        openLibraries.add(thisLibraryName);
    }


    private final javax.swing.Action addBreedAction =
		new javax.swing.AbstractAction( "Add Species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                //makeBreedBlock(null, null);

                // Disable the AddBreed button until Okay/Cancel is clicked
                addBreed.setEnabled(false);
                // Create the species editor
                JFrame someFrame = new JFrame("Species Editor");
                someFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                SpeciesEditorPanel speciesEditorPanel = new SpeciesEditorPanel(buildPanel.getBgInfo().getBreedNames(),
                                                                               buildPanel.getBgInfo().getTraits(),
                                                                               someFrame);
                speciesEditorPanel.getOkayButton().addActionListener(new SpeciesEditorPanelOkayListener(speciesEditorPanel));
                speciesEditorPanel.getCancelButton().addActionListener(new SpeciesEditorPanelCancelListener(speciesEditorPanel));
                speciesEditorPanel.setVisible(true);
                someFrame.setResizable(false);
                someFrame.pack();
                someFrame.setVisible(true);

                // if more than 1 breed available in XML -A. (oct 5)
                //Commented this out because I'm not using breedTypeSelector anymore -Aditi (March 31, 2013)
/*                if( buildPanel.availBreeds().size() > 1 ) {
                    breedTypeSelector.showMe(buildPanel.getBgInfo());
                    if (breedTypeSelector.typedBreedType() != null) {
                        Breed breed = buildPanel.getBgInfo().getBreeds().get(0);
                        newBreed = new BreedBlock( breed, breedTypeSelector.typedBreedType(), workspace.getFrame() );
                        buildPanel.addBreed(newBreed);
                        userInput.addBreed(newBreed.plural());
                        newBreed.getParent().setComponentZOrder(newBreed, 0 );
                        new BreedDropTarget(newBreed, deltaTickTab);
                        newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
                    }
                }
                    else if( breedTypeSelector.selectedBreedType() != null ) {

                        for( Breed breed : buildPanel.getBgInfo().getBreeds() ) {
                            if (breed.plural()  == breedTypeSelector.selectedBreedType()) {
                                newBreed = new BreedBlock( breed, breed.plural(), workspace.getFrame() );

                                buildPanel.addBreed(newBreed);
                                userInput.addBreed(newBreed.plural());
                                newBreed.getParent().setComponentZOrder(newBreed, 0 );
                                new BreedDropTarget(newBreed, deltaTickTab);
                                newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
                            }
                        }
                } else {*/
            }
                //newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));
            //}
      };

    public BreedBlock makeBreedBlock(String plural, String setupNumber) {
        BreedBlock newBreed;// = new BreedBlock();
        buildPanel.removeRect();
        Breed breed = buildPanel.availBreeds().get(0);

        if( buildPanel.breedCount() == 0 ) {
            newBreed = new BreedBlock( breed , breed.plural(), workspace.getFrame() );
        } else {
            newBreed = new BreedBlock( breed , breed.plural() + buildPanel.breedCount(), workspace.getFrame() );
        }

        // Add the appropriate remove/close button listener
        newBreed.getRemoveButton().addActionListener(new BreedBlockRemoveButtonListener(newBreed));

//        // Create speciesinspectorpanel for the breedblock
//        JFrame jFrame = new JFrame("Species Inspector");
//        speciesInspectorPanel = new SpeciesInspectorPanel(newBreed, jFrame);

        newBreed.setHasSpeciesInspector(true);
//        jFrame.setResizable(true);
//        jFrame.pack();
//        jFrame.setVisible(false);

//        // Add action listeners here because the action listener is a member class of deltaticktab and not accessible in breedblock
//        speciesInspectorPanel.getOkayButton().addActionListener(new SpeciesPanelOkayListener(newBreed));
//        speciesInspectorPanel.getCancelButton().addActionListener(new SpeciesPanelCancelListener(newBreed));
//        // Put the speciesinspectorpanel in the map
//        speciesInspectorPanelMap.put(newBreed, speciesInspectorPanel);

        buildPanel.addBreed(newBreed);
        userInput.addBreed(newBreed.plural());
        newBreed.getParent().setComponentZOrder(newBreed, 0 );
        new BreedDropTarget(newBreed, deltaTickTab);
        newBreed.inspectSpeciesButton.addActionListener(new SpeciesButtonListener(newBreed));

        if (plural != null) {
            newBreed.setPlural(plural);
        }
        if (setupNumber != null) {
            newBreed.setNumber(setupNumber);
        }
        contentPanel.validate();
        return newBreed;
    }

    private final javax.swing.Action removeBreedAction =
            new javax.swing.AbstractAction( "Remove Species" ) {
                public void actionPerformed( java.awt.event.ActionEvent e ) {
                   //removeBreedBlock();
                }
            };

    class BreedBlockRemoveButtonListener implements ActionListener {
        BreedBlock breedBlock;

        BreedBlockRemoveButtonListener (BreedBlock block) {
            this.breedBlock = block;
        }
        public void actionPerformed(ActionEvent e) {
            breedBlock.die();
            // Remove trait blocks associated with this breed
            for (TraitBlockNew tBlock: breedBlock.getMyTraitBlocks()) {
                libraryHolder.removeTraitBlock(tBlock);
                buildPanel.removeTrait(tBlock);
                userInput.removeTrait(tBlock.getBreedName(), tBlock.getTraitName());
            }
        }

    }

    class SpeciesButtonListener implements ActionListener {
        BreedBlock myParent;
        JFrame jFrame;

        SpeciesButtonListener(BreedBlock myParent) {
            this.myParent = myParent;
        }

        public void actionPerformed(ActionEvent e) {
            SpeciesEditorPanel panel = speciesEditorPanelHashMap.get(myParent.plural());
            panel.getTraitPreview().loadOrigSelectedTraitsMap();
            panel.updateTraitDisplay();
            panel.getMyFrame().pack();
            panel.getMyFrame().setVisible(true);

//            speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);
//            // Copy from orig traitstate map to traitstate map
//            speciesInspectorPanel.getTraitPreview().loadOrigSelectedTraitsMap();
//            speciesInspectorPanel.updateText();
//            speciesInspectorPanel.updateTraitDisplay();
//            speciesInspectorPanel.getMyFrame().pack();
//            speciesInspectorPanel.getMyFrame().validate();
//            speciesInspectorPanel.getMyFrame().setVisible(true);
        }
    }

//    // This function should/will be removed once species editor panel starts working as intended.
//    private void updateTraitBlocks(SpeciesInspectorPanel panel, BreedBlock breedBlock) {
//        // First remove all existing traits for this breed (block)
//        ArrayList<TraitBlockNew> removeBlocks = new ArrayList<TraitBlockNew>();
//        for (TraitBlockNew tBlock : panel.getMyParent().getMyTraitBlocks()) {
//            removeBlocks.add(tBlock);
//        }
//        for (TraitBlockNew tBlock : removeBlocks) {
//            libraryHolder.removeTraitBlock(tBlock);
//            buildPanel.removeTrait(tBlock);
//            userInput.removeTrait(tBlock.getBreedName(), tBlock.getTraitName());
//            //speciesInspectorPanel.getSpeciesInspector().removeTrait(tBlock.getTraitName());
//            breedBlock.removeAllTraitBlocks();
//        }
//        // Then create all traits (as selected) for this breed(block)
//        for (TraitState traitState : panel.getTraitStateMap().values()) {
//            makeTraitBlock(breedBlock, traitState);
//        }
//
//    }

    private void updateTraitBlocks(SpeciesEditorPanel panel, BreedBlock breedBlock) {
        // First remove all existing traits for this breed (block)
        ArrayList<TraitBlockNew> removeBlocks = new ArrayList<TraitBlockNew>();
        for (TraitBlockNew tBlock : breedBlock.getMyTraitBlocks()) {
            removeBlocks.add(tBlock);
        }
        for (TraitBlockNew tBlock : removeBlocks) {
            libraryHolder.removeTraitBlock(tBlock);
            buildPanel.removeTrait(tBlock);
            userInput.removeTrait(tBlock.getBreedName(), tBlock.getTraitName());
            //speciesInspectorPanel.getSpeciesInspector().removeTrait(tBlock.getTraitName());
            breedBlock.removeAllTraitBlocks();
        }
        // Then create all traits (as selected) for this breed(block)
        for (TraitState traitState : panel.getTraitStateMap().values()) {
            makeTraitBlock(breedBlock, traitState);
        }

    }

//    public class SpeciesPanelOkayListener implements ActionListener {
//        BreedBlock myParent;
//
//        SpeciesPanelOkayListener(BreedBlock myParent) {
//            this.myParent = myParent;
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            //SpeciesInspectorPanel
//            speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);
//
//            // Save origSelectedTraitsMap
//            speciesInspectorPanel.getTraitPreview().saveOrigSelectedTraitsMap();
//
//            myParent.setMaxAge(speciesInspectorPanel.getEndListSpan());
//            myParent.setMaxEnergy(speciesInspectorPanel.getHighestEnergy());
//            speciesInspectorPanel.getMyFrame().setVisible(false);
//
////            // First remove all existing traits for this breed (block)
////            ArrayList<TraitBlockNew> removeBlocks = new ArrayList<TraitBlockNew>();
////            for (TraitBlockNew tBlock : speciesInspectorPanel.getMyParent().getMyTraitBlocks()) {
////                removeBlocks.add(tBlock);
////            }
////            for (TraitBlockNew tBlock : removeBlocks) {
////                libraryHolder.removeTraitBlock(tBlock);
////                buildPanel.removeTrait(tBlock);
////                userInput.removeTrait(tBlock.getBreedName(), tBlock.getTraitName());
////                //speciesInspectorPanel.getSpeciesInspector().removeTrait(tBlock.getTraitName());
////                myParent.removeAllTraitBlocks();
////            }
////            // Then create all traits (as selected) for this breed(block)
////            for (TraitState traitState : speciesInspectorPanel.getTraitStateMap().values()) {
////                makeTraitBlock(myParent, traitState);
////            }
//
//            // Update the trait blocks (add, remove or update variations)
//            // because of possible changes from species inspector
//            updateTraitBlocks(speciesInspectorPanel, myParent);
//
//            // Update existing behavior blocks because traits and variations may have changed
//            myParent.updateMyBehaviorBlocks();
//
//            myParent.getTraitLabels().clear();
//            for (String traitLabel : speciesInspectorPanel.getTraitPreview().getLabelPanel().getSelectedLabels()) {
//                myParent.addToTraitLabels(traitLabel);
//            }
//                //TODO: this is a hard-coded hack because "trait" becomes null. Fix it -Aditi (Feb 22, 2013)
//        }
//    }
//
    public class SpeciesPanelCancelListener implements ActionListener {
    BreedBlock myParent;

    SpeciesPanelCancelListener(BreedBlock myParent) {
        this.myParent = myParent;
    }

    public void actionPerformed(ActionEvent e) {
        //SpeciesInspectorPanel
        speciesInspectorPanel = speciesInspectorPanelMap.get(myParent);
        speciesInspectorPanel.getTraitPreview().loadOrigSelectedTraitsMap();
        speciesInspectorPanel.updateTraitDisplay();
        speciesInspectorPanel.getMyFrame().setVisible(false);
    }
}

    public class SpeciesEditorPanelOkayListener implements ActionListener {
        // The Species Editor Panel corresponding to this listener
        SpeciesEditorPanel sePanel;

        SpeciesEditorPanelOkayListener(SpeciesEditorPanel panel) {
            this.sePanel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            // Pointer to a breed block
            // This will be set by a new one or retrived from an existing one
            BreedBlock breedBlock;
            // Check if new breedBlock needs to be created
            if (sePanel.getMakeNewBreedBlock()) {
                // This panel has been created from Add Species button
                // Enable the button
                addBreed.setEnabled(true);
                // Read information from the panel
                String breedName = new String(sePanel.getMyBreedName());
                String setupNumber = new String(sePanel.getMySetupNumber());
                String maxNumber = new String(sePanel.getMyMaxNumber());
                // Create the breed block
                breedBlock = makeBreedBlock(breedName, setupNumber);
                // Set the panel's breed block to the newly created one
                sePanel.setMyBreedBlock(breedBlock);
                // Indicate that next time, a new breed block does not need to be created
                sePanel.setMakeNewBreedBlock(false);
                // Add panel to HashMap
                speciesEditorPanelHashMap.put(breedName, sePanel);
            }

            // Get the breed block
            breedBlock = sePanel.getMyBreedBlock();
            // Check if breed name has changed, and if the change is valid
            if (!breedBlock.plural().equalsIgnoreCase(sePanel.getMyBreedName())) {
                // Check if change is valid
                String newBreedName = sePanel.getMyBreedName();
                if (!buildPanel.breedExists(newBreedName)) {
                    // New selected breed(name) is okay
                    // Update HashMap
                    speciesEditorPanelHashMap.remove(sePanel);
                    speciesEditorPanelHashMap.put(newBreedName, sePanel);
                    // Update plural (textfield) of breedblock
                    breedBlock.setPlural(newBreedName);
                    breedBlock.setName(newBreedName);
                }
                else {
                    // Breed already exists -- this change is invalid
                    String message = new String("Oops! The species you selected already exists. Choose a different species.");
                    JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            // Save origSelectedTraitsMap
            sePanel.getTraitPreview().saveOrigSelectedTraitsMap();
            // Update the trait blocks
            updateTraitBlocks(sePanel, breedBlock);
            // Update existing behavior blocks because traits and variations may have changed
            breedBlock.updateMyBehaviorBlocks();
            // Set the shape
            breedBlock.setBreedShape(sePanel.getMyBreedShape());
            // Set the color
            breedBlock.setColorName(sePanel.getMyBreedColorName());
            // Set the setup number
            breedBlock.setSetupNumber(sePanel.getMySetupNumber());
            // Set the max number
            breedBlock.setMaxNumber(sePanel.getMyMaxNumber());

            // Now hide the panel
            sePanel.getMyFrame().setVisible(false);
//
//            myParent.getTraitLabels().clear();
//            for (String traitLabel : speciesInspectorPanel.getTraitPreview().getLabelPanel().getSelectedLabels()) {
//                myParent.addToTraitLabels(traitLabel);
//            }
//            //TODO: this is a hard-coded hack because "trait" becomes null. Fix it -Aditi (Feb 22, 2013)
        }
    }
    public class SpeciesEditorPanelCancelListener implements ActionListener {
        BreedBlock myParent;
        SpeciesEditorPanel sePanel;

        SpeciesEditorPanelCancelListener(SpeciesEditorPanel panel) {
            sePanel = panel;
        }

        public void actionPerformed(ActionEvent e) {
            if (sePanel.getMakeNewBreedBlock()) {
                // This panel has been created from Add Species button
                // Enable the button
                addBreed.setEnabled(true);
                // Hide this frame
                sePanel.getMyFrame().setVisible(false);
            }
            else {
                // This panel has been created/called from Species Inspector Button
                sePanel.getTraitPreview().loadOrigSelectedTraitsMap();
                sePanel.updateTraitDisplay();
                sePanel.getMyFrame().setVisible(false);
            }
        }
    }

    private final javax.swing.Action speciesTryAction =
		new javax.swing.AbstractAction( "Try Species" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                makeBreedBlock(null, null);
            }
        };

    public void makeTraitBlock(BreedBlock bBlock, TraitState traitState) {
        TraitBlockNew traitBlock = new TraitBlockNew(bBlock, traitState, traitState.getVariationHashMap(), traitState.getVariationsValuesList());
        traitBlock.setMyParent(bBlock);
        traitBlock.setBreedName(bBlock.plural());

        //speciesInspectorPanel.getSpeciesInspector().addToSelectedTraitsList(traitState);
        userInput.addTraitAndVariations(bBlock.plural(), traitState.getNameTrait(), traitState.getVariationsList());
        buildPanel.addTrait(traitBlock);
        libraryHolder.addTraittoTab(traitBlock, buildPanel.getMyTraits().size());
        deltaTickTab.addDragSource(traitBlock);
        bBlock.addTraitBlocktoList(traitBlock);

        contentPanel.validate();
    }

    public SpeciesInspectorPanel getSpeciesInspectorPanel(BreedBlock bBlock) {
        return speciesInspectorPanelMap.get(bBlock);
    }
    public PlotBlock makePlotBlock(boolean isHisto) {
        PlotBlock newPlotBlock = new PlotBlock(isHisto);
        buildPanel.removeRect();
        buildPanel.addPlot( newPlotBlock );
        newPlotBlock.getParent().setComponentZOrder(newPlotBlock, 0 );
        if (isHisto) {
            new HistoDropTarget(newPlotBlock);
        }
        else {
            new PlotDropTarget(newPlotBlock);
        }
        newPlotBlock.getPlotNameField().addFocusListener(plotNameFieldListener);
        newPlotBlock.validate();
        contentPanel.validate();
        getParent().repaint();
        return newPlotBlock;
    }
    public MonitorBlock makeMonitorBlock() {
        MonitorBlock monitorBlock = new MonitorBlock();
        buildPanel.removeRect();
        buildPanel.addMonitor(monitorBlock);
        monitorBlock.showRemoveButton();
        new MonitorDropTarget(monitorBlock);
        monitorBlock.getParent().setComponentZOrder(monitorBlock, 0);
        monitorBlock.validate();
        contentPanel.validate();
        getParent().repaint();
        return monitorBlock;
    }

    private final javax.swing.Action addPlotAction =
            new javax.swing.AbstractAction( "Add Line Graph" ) {
                public void actionPerformed( java.awt.event.ActionEvent e ) {
                    // Line graph, so parameter is false
                    makePlotBlock(false);
                }
            };
    private final javax.swing.Action addHistoAction =
            new javax.swing.AbstractAction( "Add Bar Graph" ) {
                public void actionPerformed( java.awt.event.ActionEvent e ) {
                    // histogram, so parameter is true
                    makePlotBlock(true);
                }
            };

    private final Action addMonitorAction =
            new AbstractAction( "Add Monitor") {
                public void actionPerformed ( ActionEvent e ) {
                    makeMonitorBlock();
                }
            };

    public class PlotNameFieldListener implements FocusListener {
        boolean messageDisplayed = false;
        public void focusLost(FocusEvent e) {
            if (!messageDisplayed) {
                checkAndDisplayMessage(e);
            }
            messageDisplayed = false;
        }

        public void focusGained(FocusEvent e) {
        }

        void checkAndDisplayMessage(FocusEvent e) {
            messageDisplayed = true;
            for (int i = 0; i < buildPanel.getMyPlots().size(); i++) {
                PlotBlock plotBlock = buildPanel.getMyPlots().get(i);
                for (int j = 0; j < buildPanel.getMyPlots().size(); j++) {
                    PlotBlock histogramBlock = buildPanel.getMyPlots().get(j);
                    if ((i != j) &&
                        histogramBlock.isHistogram() &&
                        !plotBlock.isHistogram()) {
                        if (plotBlock.getPlotName().equalsIgnoreCase(histogramBlock.getPlotName())) {
                            String message = new String("Oops! You have a Plot and a Histogram with the same name.");
                            ((JTextField) e.getSource()).selectAll();
                            ((JTextField) e.getSource()).requestFocus();
                            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private final javax.swing.Action clearAction =
		new javax.swing.AbstractAction( "Clear" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                buildPanel.clear();
                addBreed.setEnabled(true);
                clearPlots();
                variationSelector.getVariationList().clear();
                for (TraitBlockNew tBlock : libraryHolder.getTraitBlocks()) {
                    libraryHolder.removeTraitBlock(tBlock);
                }
                contentPanel.repaint();
        }
    };


    private final Action diveInAction =
            new AbstractAction( "Step in") {
                public void actionPerformed( java.awt.event.ActionEvent e ) {
                    makeDiveInBlock();
                }
            };

    public DiveInBlock makeDiveInBlock() {
        DiveInBlock diveInBlock = new DiveInBlock(buildPanel.getBgInfo().getDiveIns().get(0), workspace.getFrame());
        DiveInDropTarget diveInDropTarget = new DiveInDropTarget(diveInBlock);
        buildPanel.addDiveIn( diveInBlock );
        buildPanel.removeRect();

        diveInBlock.validate();
        contentPanel.validate();
        getParent().repaint();
        return diveInBlock;
    }

    private final javax.swing.Action chgEnvtAction =
		new javax.swing.AbstractAction( "Add environment" ) {
            public void actionPerformed( java.awt.event.ActionEvent e ) {
                envtTypeSelector.showMe(buildPanel.getBgInfo());
                EnvtBlock newEnvt;
                for ( Envt envt: buildPanel.getBgInfo().getEnvts() ) {
                    if ( envtTypeSelector.selectedEnvt() != null ) {
                        if ( envtTypeSelector.selectedEnvt() == envt.nameEnvt()) {
                            newEnvt = new EnvtBlock (envt);
                            new EnvtDropTarget( newEnvt, deltaTickTab );
                            buildPanel.addEnvt(newEnvt);
                        }
                        contentPanel.validate();
                    };
                };
            }
        };

    private final Action toBuildBlock =
            new javax.swing.AbstractAction( "Build operator block" ) {
                public void actionPerformed ( ActionEvent e ) {
                    OperatorBlock newOBlock;
                    obBuilder.showMe(userInput);
                    if ( obBuilder.check()  == true ) {
                        newOBlock = new OperatorBlock( obBuilder.selectedBreed(), obBuilder.selectedTrait(),
                                obBuilder.selectedTrait2(),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait()),
                                userInput.getVariations(obBuilder.selectedBreed(), obBuilder.selectedTrait2()));
                        libraryHolder.addOperatortoTab(newOBlock);
                        deltaTickTab.addDragSource(newOBlock);
                    }
                }
            };

    public class XMLFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equalsIgnoreCase("xml")) {
                    return true;
                }
                else {
                    return false;
                }
            }

            return false;
        }

        public String getDescription() {
            return "XML files (.xml)";
        }

        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }

    private final Action opensaveModelAction =
            new AbstractAction() {
                JFileChooser fileChooser = new JFileChooser();

                public void actionPerformed (ActionEvent e) {
                    fileChooser.addChoosableFileFilter(new XMLFilter());
                    fileChooser.setAcceptAllFileFilterUsed(false);

                    if (e.getSource() == openModelButton) {
                        int returnVal = fileChooser.showOpenDialog(DeltaTickTab.this);

                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            openModel(file);
                        }

                        //Handle save button action.
                    } else if (e.getSource() == saveModelButton) {
                        int returnVal = fileChooser.showSaveDialog(DeltaTickTab.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            saveModel(file);
                        }
                    }
                }
    };

    public void saveModel(File modelFile) {
        deltaTickModelParser.saveModel(modelFile);
    }

    public void openModel(File modelFile) {
        deltaTickModelParser.openModel(modelFile);
    }

    public void populateProcedures() {
        pt.innerSource( buildPanel.unPackAsCode() );
        //pt.innerSource( libraryHolder.unPackAsCode() );
        //pt.select(0,pt.innerSource().length());
        pt.setIndenter(true);
    }

    public void populateInterface() {
        if (interfaceCount == 0) {
        org.nlogo.window.Widget setupWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(setupWidget, 0, 0, true, false);
        if (setupWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) setupWidget;
            button.displayName("setup");
            button.wrapSource("setup");
        }
        org.nlogo.window.Widget goWidget = interfacePanel.makeWidget("BUTTON",false);
        interfacePanel.addWidget(goWidget, 60, 0, true, false);

        if (goWidget instanceof org.nlogo.window.ButtonWidget) {
          org.nlogo.window.ButtonWidget button =
              (org.nlogo.window.ButtonWidget) goWidget;
            button.displayName("go");
            button.wrapSource("go");
            button.setForeverOn();
        }

      /*
            //Commented out because I don't want "draw" and "envtChooser" any more -Aditi (March 9, 2013)
        org.nlogo.window.Widget drawWidget = interfacePanel.makeWidget("BUTTON",false);
            interfacePanel.addWidget(drawWidget, 0, 130, true, false);
            if (drawWidget instanceof org.nlogo.window.ButtonWidget) {
                org.nlogo.window.ButtonWidget button =
                    (org.nlogo.window.ButtonWidget) drawWidget;
                button.displayName("draw");
                button.wrapSource("draw");
                button.setForeverOn();
        }
        org.nlogo.window.Widget envtChooserWidget = interfacePanel.makeWidget("CHOOSER",false);
        interfacePanel.addWidget(envtChooserWidget, 0, 100, true, false);
        //org.nlogo.window.ButtonWidget buttonWidget = interface
        if (envtChooserWidget instanceof org.nlogo.window.ChooserWidget) {
          org.nlogo.window.ChooserWidget chooser =
              (org.nlogo.window.ChooserWidget) envtChooserWidget;
            chooser.displayName("environment");
            chooser.nameWrapper("environment");
            LogoListBuilder choicesList = new LogoListBuilder();
            choicesList.add("grass");
            choicesList.add("water");
            chooser.setChoices(choicesList.toLogoList());
        }
      */
            interfacePanel.clearNewWidget();
            interfaceCount++;
        }
    }




    public void populateMutationSlider() {
        boolean putNoteWidget = false;

        for (BreedBlock bBlock : buildPanel.getMyBreeds()) {
            if (bBlock.getReproduceUsed() && buildPanel.getMyTraits().size() > 0) {
                putNoteWidget = true;
                for (TraitBlockNew tBlock : bBlock.getMyTraitBlocks()) {
                    String sliderName = tBlock.getMyParent().plural() + "-" + tBlock.getTraitName() + "-mutation";
                    SliderWidget sliderWidget = ((SliderWidget) interfacePanel.makeWidget("SLIDER", false));

                    // Set value from previous instance or default
                    double value = (mutationSliderValues.containsKey(sliderName)) ? mutationSliderValues.get(sliderName) : MUTATION_SLIDER_DEFAULT_VALUE;
                    sliderWidget.valueSetter(value);

                    // Set name
                    sliderWidget.name_$eq(sliderName);

                    WidgetWrapper ww = interfacePanel.addWidget(sliderWidget, 0, (120 + interfaceSliderCount * 40), true, false);

                    mutationSliderWidgets.put(sliderName, ww);
                    interfacePanel.clearNewWidget();
                    interfaceSliderCount++;
                }
            }
            revalidate();
        }
        // Clear the sliderValuesHashMap
        mutationSliderValues.clear();

        //make a note only once (March 29, 2013)
        if (putNoteWidget) {
            NoteWidget noteWidget = ((NoteWidget) interfacePanel.makeWidget("NOTE", false));
            WidgetWrapper widgetw = interfacePanel.addWidget(noteWidget, 0, 80, true, false);
            String note = "Chance of mutation in";
            noteWidget.setBounds(0, 80, 20, 30);
            noteWidget.text_$eq(note);
            //noteWidget.validate();
            noteWidgets.put("MutationNote", widgetw);
        }
    }

    public void removeMutationSlider() {
        for (Map.Entry<String, WidgetWrapper> entry : mutationSliderWidgets.entrySet()) {
            String p = entry.getKey();
            WidgetWrapper w = entry.getValue();

            // Record the value of the slider
            mutationSliderValues.put(p, ((SliderWidget) w.widget()).value() );

            // Remove the widget from interface panel
            interfacePanel.removeWidget(w);
        }
        mutationSliderWidgets.clear();

        for (Map.Entry<String, WidgetWrapper> entry : noteWidgets.entrySet()) {
            WidgetWrapper w = entry.getValue();

            //noteWidgets.remove(w);
            interfacePanel.removeWidget(w);
        }
        noteWidgets.clear();
        revalidate();
    }

    public void populateCarryingCapacitySlider() {
        // First remove carrying capacity sliders
        for (Map.Entry<String, WidgetWrapper> entry : carryingCapacitySliderWidgets.entrySet()) {
            String p = entry.getKey();
            WidgetWrapper w = entry.getValue();

            // Record the value of the slider
            carryingCapacitySliderValues.put(p, ((SliderWidget) w.widget()).value() );

            // Remove the widget from interface panel
            interfacePanel.removeWidget(w);
        }
        carryingCapacitySliderWidgets.clear();
        // All carrying capacity sliders removed

        // Now re-populate carrying capacity sliders
        for (BreedBlock bBlock : buildPanel.getMyBreeds()) {
            if (bBlock.getReproduceUsed()) {
                // Put the slider
                String sliderName = "max-number-of-" + bBlock.plural();
                SliderWidget sliderWidget = ((SliderWidget) interfacePanel.makeWidget("SLIDER", false));

                sliderWidget.minimumCode_$eq(CARRYING_CAPACITY_SLIDER_MIN_VALUE.toString());
                sliderWidget.maximumCode_$eq(CARRYING_CAPACITY_SLIDER_MAX_VALUE.toString());

                double value = (carryingCapacitySliderValues.containsKey(sliderName)) ? carryingCapacitySliderValues.get(sliderName) : CARRYING_CAPACITY_SLIDER_DEFAULT_VALUE;
                sliderWidget.valueSetter(value);

                // Set name
                sliderWidget.name_$eq(sliderName);

                WidgetWrapper ww = interfacePanel.addWidget(sliderWidget, 0, (120 + interfaceSliderCount * 40), true, false);
                interfaceSliderCount++;

                carryingCapacitySliderWidgets.put(sliderName, ww);
            }
        }
        revalidate();
        // Clear the sliderValuesHashMap - this will be populated with latest values when the tab is switched back
        carryingCapacitySliderValues.clear();
    }

    public void populateLabelChooser() {
        // Remove chooserWidget
        for (WidgetWrapper w : chooserWidgets.values()) {
            interfacePanel.remove(w);
        }
        chooserWidgets.clear();

        // Now populate labels
        for (BreedBlock bBlock : buildPanel.getMyBreeds()) {
            boolean putChooser = false;
            String labelOptions = "\"none\"";
            for (TraitBlockNew tBlock : bBlock.getMyTraitBlocks()) {
                labelOptions += "\"" + tBlock.getTraitName() + "\"";
                putChooser = true;
            }
            if (putChooser) {
                ChooserWidget chooserWidget = ((ChooserWidget) interfacePanel.makeWidget("CHOOSER", false));
                String chooserWidgetName = new String(bBlock.plural() + "-label");
                chooserWidget.name(chooserWidgetName);
                chooserWidget.choicesWrapper(labelOptions);

                WidgetWrapper ww = interfacePanel.addWidget(chooserWidget, 0, (120 + interfaceSliderCount * 60), true, false);
                interfaceSliderCount++;

                chooserWidgets.put(chooserWidgetName, ww);
            }
        }
        interfacePanel.clearNewWidget();
    }

    public void populateMonitors() {
        int MonitorCount = 0;
        try {
            for (MonitorBlock mBlock : buildPanel.getMyMonitors()) {
                int x = 100;
                int y = 200;
                for (QuantityBlock qBlock : mBlock.getMyBlocks()) {

                    if (qBlock.getHisto() == true) {
                        String trait = qBlock.getHistoTrait();
                        String breed = qBlock.getHistoBreed();
                        ArrayList<String> variations = new ArrayList<String>();
                        for (TraitBlockNew tBlock : buildPanel.getMyTraits()) {
                            if (tBlock.getName().equalsIgnoreCase(trait)) {
                                for (Variation var : tBlock.getVariationHashMap().values()) {
                                    String code = "";
                                    code += "count " + breed + " with [" + trait + " = " + var.value + "]";
                                    org.nlogo.window.MonitorWidget monitorWidget = ((MonitorWidget) interfacePanel.makeWidget("MONITOR", false));
                                    String monitorWidgetName = new String("name");
                                    monitorWidget.name(monitorWidgetName);
                                    monitorWidget.wrapSource(code);

                                    WidgetWrapper ww = interfacePanel.addWidget(monitorWidget, x, y, true, false);
                                    y = y + 10;
                                }
                            }
                        }
                    }
                    else if (qBlock.getHisto() == false) {
                        org.nlogo.window.MonitorWidget monitorWidget = ((MonitorWidget) interfacePanel.makeWidget("MONITOR", false));
                        String monitorWidgetName = new String("name");
                        monitorWidget.name(monitorWidgetName);
                        String code = qBlock.getMonitorCode();
                        monitorWidget.wrapSource(code);

                        WidgetWrapper ww = interfacePanel.addWidget(monitorWidget, 40, 80, true, false);

                    }
                }
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        interfacePanel.clearNewWidget();
        revalidate();

    }

    public void populatePlots() {
        int plotCount = 0;
        try {
            for (PlotBlock plotBlock : buildPanel.getMyPlots()) {
                if (plotWrappers.containsKey(plotBlock.getPlotName()) == false) {
                    // Plot not previously present ==> new plot must be created
                    org.nlogo.window.Widget plotWidget = interfacePanel.makePlotWidget(plotBlock.getPlotName());

                    WidgetWrapper ww = interfacePanel.addWidget(plotWidget, 660 + ((plotCount/3) * 200), 10 + ((plotCount%3)*160), true, false);

                    plotWidget.displayName(plotBlock.getPlotName());

                    org.nlogo.plot.Plot newPlot = workspace.plotManager().getPlot(plotBlock.getPlotName());

                    plotBlock.setNetLogoPlot(newPlot);

                    plotWrappers.put(plotBlock.getPlotName(), ww);

                    // First time creating plot.
                    // Save pen names
                    for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                        // Now save the new pen name
                        quantBlock.setSavedPenName();
                        ((AbstractPlotWidget) plotWidget).xLabel(quantBlock.getXLabel());
                        ((AbstractPlotWidget) plotWidget).yLabel(quantBlock.getYLabel());
                        //plot.legendIsOpen
                    }

                    // Create new pens
                    for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                        if (newPlot.getPen(quantBlock.getPenName()).toString().equals("None")) {
                            PlotPen plotPen = newPlot.createPlotPen(quantBlock.getPenName(), false);
                            plotPen.updateCode(quantBlock.getPenUpdateCode());
                            ((PlotWidget) plotWidget).xLabel(quantBlock.getXLabel());
                            ((PlotWidget) plotWidget).yLabel(quantBlock.getYLabel());
                        }
                    }
                }
                else {
                    // Plot already exists, just recalculate its position
                    WidgetWrapper ww = plotWrappers.get(plotBlock.getPlotName());
                    ww.setLocation(660 + ((plotCount/3) * 200), 10 + ((plotCount%3)*160));

                    // Clear renamed plot pens
                    for (QuantityBlock quantBlock : plotBlock.getMyBlocks()) {
                        if (!quantBlock.getSavedPenName().equalsIgnoreCase("")) {
                            // Previous pen name had been saved
                            if (!quantBlock.getSavedPenName().equalsIgnoreCase(quantBlock.getPenName())) {
                                // Name has changed
                                plotBlock.removePen(quantBlock.getSavedPenName());
                            }
                        }
                        // Now save the new pen name
                        quantBlock.setSavedPenName();
                    }

                    // Make sure plot pens are up to date
                    org.nlogo.plot.Plot thisPlot = workspace.plotManager().getPlot(plotBlock.getPlotName());
                    //thisPlot.removeAllPens();
                    //TODO: Access plot editor
                    for (QuantityBlock qBlock : plotBlock.getMyBlocks()) {
                        if (thisPlot.getPen(qBlock.getPenName()).toString().equals("None")) {
                            PlotPen newPlotPen = thisPlot.createPlotPen(qBlock.getPenName(), false);
                            newPlotPen.updateCode(qBlock.getPenUpdateCode());
                            newPlotPen.plot(thisPlot.xMax(), thisPlot.yMax());
                            ((PlotWidget) ww.widget()).xLabel(qBlock.getXLabel());
                            ((PlotWidget) ww.widget()).yLabel(qBlock.getYLabel());
                            //newPlotPen._hidden = true;
                        }
                    }
                }
                // Proceed to next plot
                plotCount++;
            }

            // The following code may be unnecessary because histograms are contained in builPanel.getMyPlots()
            for (HistogramBlock hBlock : buildPanel.getMyHisto().subList(interfaceHistoCount, buildPanel.getMyHisto().size())) {
                org.nlogo.window.Widget plotWidget = interfacePanel.makeWidget("Plot", false);
                interfacePanel.addWidget(plotWidget, 5, 50, true, false);
                plotWidget.displayName(hBlock.getHistogramName());

                org.nlogo.plot.Plot newPlot = workspace.plotManager().getPlot("plot 1");
                PlotPen plotPen = newPlot.getPen("default").get();
                for (QuantityBlock quantBlock : hBlock.getMyBlocks()) {
                    //String penName = "";
                    plotPen.updateCode(quantBlock.getPenUpdateCode());
                }
                interfaceHistoCount++;
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        revalidate();
    }

    public void removePlots() {

        try {
            for (Map.Entry<String, WidgetWrapper> entry : plotWrappers.entrySet()) {
                String p = entry.getKey();
                WidgetWrapper w = entry.getValue();
                if (buildPanel.plotExists(p) == false) {
                    interfacePanel.removeWidget(w);
                    plotWrappers.remove(p);
                    workspace.plotManager().forgetPlot(workspace.plotManager().getPlot(p));
                }
            }
        }
        catch ( Exception ex ) {
            System.out.println(ex.getMessage());
        }
        revalidate();
    }

    public void removePlotPens() {

    }

    // ordering of the buttons on the deltatick ToolBar -A. (sept 8)
    org.nlogo.swing.ToolBar getToolBar() {
		return new org.nlogo.swing.ToolBar() {
            @Override
            public void addControls() {
                loadLibrary = new JButton( loadAction );
                this.add(loadLibrary);
                //this.add( new JButton( loadAction ) ) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                //this.add( new JButton( loadAction2 ) );
                addBreed = new JButton( addBreedAction );
                addBreed.setEnabled(false);
                this.add(addBreed) ;


                addDiveIn = new JButton(diveInAction);
                addDiveIn.setEnabled(false);
                this.add(addDiveIn);

                /*speciesTry = new JButton( speciesTryAction );
                speciesTry.setEnabled(true);
                this.add(speciesTry);*/

                addTrackSpecies = new JButton("Track Species");
                addTrackSpecies.addMouseListener(new TrackSpeciesListener());
                this.add(addTrackSpecies);
                addTrackSpecies.setEnabled(false);

                trackSpeciesPopUp = new JPopupMenu();
                addHisto = new JMenuItem( addHistoAction );
                trackSpeciesPopUp.add(addHisto);

                addPlot = new JMenuItem( addPlotAction );
                trackSpeciesPopUp.add(addPlot);

                addMonitor = new JMenuItem ( addMonitorAction );
                trackSpeciesPopUp.add(addMonitor);

                //this.add(addMonitor);
                //addEnvt = new JButton ( chgEnvtAction );
                //addEnvt.setEnabled(false);
                //this.add(addEnvt) ;
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                saveModelButton = new JButton();
                saveModelButton.setAction(opensaveModelAction);
                saveModelButton.setText("Save Model");
                this.add(saveModelButton);
                saveModelButton.setEnabled(false);
                openModelButton = new JButton();
                openModelButton.setAction(opensaveModelAction);
                openModelButton.setText("Open Model");
                this.add(openModelButton);
                //buildBlock = new JButton( toBuildBlock );
                //this.add( buildBlock );
                this.add( new org.nlogo.swing.ToolBar.Separator() ) ;
                addClear = new JButton(clearAction);
                addClear.setEnabled(false);
                this.add(addClear);
                //this.add( new JButton( procedureAction ) ) ;
            }
        } ;
	}


    //this method populates the library panel with all the blocks from the XML (read in Library Reader) except traitBlock
    //(Feb 16, 2012)
    public JPanel getLibraryPanel() {
        return libraryPanel;
    }

    public BuildPanel getBuildPanel() {
        return buildPanel;
    }

    public LibraryHolder getLibraryHolder() {
        return libraryHolder;
    }

    public TraitSelectorOld getTraitSelector() {
        return traitSelector;
    }

    public VariationSelector getVariationSelector() {
        return variationSelector;
    }

    // this procedure might be the one responsible to updating code everytime tab is switched - A (May 4)
    public void handle( Events.SwitchedTabsEvent event ) {
        if( event.oldTab == this ) {
            boolean error = checkErrors();
            if (error) {
                //event.oldTab.requestFocus();
                return;
            }
            populateProcedures();
            pt.setIndenter(true);
            pt.select(0, pt.innerSource().length() );
            // pt.getIndenter().handleTab();
            pt.select(0,0);
            populateInterface();
            removePlots();
            populatePlots();
            //removeMutationSlider();
            interfaceSliderCount = 0;
            //populateMutationSlider();
            populateCarryingCapacitySlider();
            populateLabelChooser();
            populateMonitors();
            new org.nlogo.window.Events.CompileAllEvent()
				.raise( DeltaTickTab.this ) ;
        }
    }

    public void run( String command ) {
        try {
            workspace.evaluateCommands(defaultOwner, command);
        } catch (CompilerException e) {
            throw new RuntimeException(e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void go() {
        run("go");
    }

    public void setup() {
        run("setup");
    }

    public void clearLibrary() {
        libraryPanel.removeAll();
        buildPanel.clear();
        clearPlots();
        buildPanel.getBgInfo().clear();
        libraryHolder.removeAll();
        //breedTypeSelector.clear();
        libraryPanel.repaint();
    }


    public void clearPlots() {
        workspace.plotManager().forgetAll();
    }

    public void setPlotsAlive( boolean alive ) {
        plotsAlive = alive;
    }

    public String toXml() {
        return buildPanel.saveAsXML();
    }

    public void load( String theXml ) {
        //TODO...

    }

    public String libraryName() {
        return buildPanel.getBgInfo().getLibrary();
    }

    public LibraryReader getLibraryReader() {
        return this.libraryReader;
    }
    private boolean checkErrors() {
        if (checkForErrors) {
            // Check 1:
            // Check if a trait block that should be added to a behavior block, isn't added
            // For each breed
            for (BreedBlock breedBlock : buildPanel.getMyBreeds()) {
                for (BehaviorBlock behaviorBlock : breedBlock.getMyBehaviorBlocks()) {
                    for (String traitName: behaviorBlock.getApplicableTraits()) {
                        if (breedBlock.hasTrait(traitName) &&
                                !behaviorBlock.getIsTrait()) {
                            String message = new String("Behavior block " + behaviorBlock.getName() + " needs a trait block.");
                            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.ERROR_MESSAGE);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    class TrackSpeciesListener implements MouseListener {
        public void mouseReleased (MouseEvent e) {
            trackSpeciesPopUp.show(e.getComponent(), e.getX(), e.getY());

        }
        public void mouseExited (MouseEvent e) {
        }
        public void mouseClicked (MouseEvent e) {
        }
        public void mousePressed (MouseEvent e) {
        }
        public void mouseEntered (MouseEvent e) {
        }
    }




//class PopupListener extends MouseAdapter {
//    JPopupMenu trackSpeciesPopUp;
//    JMenuItem menuItem;
//
//    PopupListener(JPopupMenu popupMenu) {
//        trackSpeciesPopUp = popupMenu;
//        menuItem = new JMenuItem("ice");
//        trackSpeciesPopUp.add(menuItem);
//        JPanel panel = new JPanel();
//        panel.add(trackSpeciesPopUp);
//
//    }
//
//      public void mousePressed(MouseEvent ev) {
//        if (ev.isPopupTrigger()) {
//          trackSpeciesPopUp.show(ev.getComponent(), ev.getX(), ev.getY());
//        }
//      }
//
//      public void mouseReleased(MouseEvent ev) {
//        if (ev.isPopupTrigger()) {
//          trackSpeciesPopUp.show(ev.getComponent(), ev.getX(), ev.getY());
//        }
//      }
//
//      public void mouseClicked(MouseEvent ev) {
//      }
//    }
}


