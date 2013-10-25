package org.nlogo.deltatick.dialogs;

import org.nlogo.deltatick.xml.ModelBackgroundInfo;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwilkerson
 * Date: May 11, 2010
 * Time: 5:21:29 PM
 * To change this template use File | Settings | File Templates.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewJDialog3.java
 *
 * Created on May 11, 2010, 5:17:33 PM
 */

/**
 * @author mwilkerson
 */
public class BreedTypeSelector extends javax.swing.JDialog {

    private javax.swing.JButton cancel;
    private javax.swing.JButton add;
    private javax.swing.JLabel infoText;
    private javax.swing.JList breedList;
    private javax.swing.JScrollPane jScrollPane1;
    private JButton addYourOwn;
    private JTextField yourOwnBreed;
    private JButton addTrait;

    private javax.swing.JDialog thisDialog = this;

    // constructor for BreedTypeSelector
    public BreedTypeSelector(java.awt.Frame parent) {
        super(parent, true);
        initComponents();

        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                breedList.clearSelection();
                thisDialog.setVisible(false);
            }
        });

        add.setText("Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thisDialog.setVisible(false);
            }
        });
    }


    public void showMe(ModelBackgroundInfo backgroundInfo) {
        final String[] strings = backgroundInfo.getBreedNames();
        breedList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return strings.length;
            }

            public Object getElementAt(int i) {
                return strings[i];
            }
        });
        jScrollPane1.setViewportView(breedList);
        yourOwnBreed.setText("");
        this.setVisible(true);
    }

    public String selectedBreedType() {
        return (String) breedList.getSelectedValue();
    }

    public String typedBreedType() {
        return yourOwnBreed.getText();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        infoText = new javax.swing.JLabel("What kind of actors do you want to add?");
        cancel = new javax.swing.JButton();
        add = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        breedList = new javax.swing.JList();
        addYourOwn = new JButton();
        yourOwnBreed = new JTextField();
        //addTrait = new JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(24, 24, 24)
                                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))

                                        .add(yourOwnBreed)
                                .add(infoText))

                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(115, Short.MAX_VALUE)
                                .add(add)
                                        //.add(addYourOwn)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(cancel)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(yourOwnBreed)
                                .add(infoText)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(cancel)
                                                //.add(addYourOwn)
                                        .add(add))
                                .add(26, 26, 26))
        );

        pack();
    }// </editor-fold>
}
