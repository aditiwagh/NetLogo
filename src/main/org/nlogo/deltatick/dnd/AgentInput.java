package org.nlogo.deltatick.dnd;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 8/11/12
 * Time: 10:59 PM
 * To change this template use File | Settings | File Templates.
 */

public class AgentInput extends PrettyInput {
    public AgentInput(Component parent) {
        super(parent);

        setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(parent.getBackground().darker()),
                javax.swing.BorderFactory.createEmptyBorder(1, 2, 0, 0)
        ));
        //setBackground( Color.white );

        setFont(new java.awt.Font("Arial", 1, 11));
        setSize(this.getWidth(), 10);
        this.createToolTip();
        this.setToolTipText("<html><font size=\"4\">Who?</font></html>");
}
}
