package ui

import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class MotionEditorBase : JPanel() {

    val cbxLoop: JCheckBox
    val txt1: JTextArea
    val btnSend = JButton("SEND")
    val btnClear = JButton("CLEAR")
    val btnStop = JButton("STOP")

    init {
        layout = BorderLayout()
//        setBounds(0, 0, 200, 600)

        cbxLoop = JCheckBox("ループ")

        txt1 = JTextArea()
        val spane = JScrollPane(txt1)
        val sPanel = JPanel()
        sPanel.layout = FlowLayout()
        sPanel.add(btnSend)
        sPanel.add(btnClear)
        sPanel.add(btnStop)

        add(cbxLoop, BorderLayout.NORTH)
        add(spane, BorderLayout.CENTER)
        add(sPanel, BorderLayout.SOUTH)
    }
}