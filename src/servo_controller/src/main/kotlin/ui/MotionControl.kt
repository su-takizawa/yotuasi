package ui

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

data class MotionControlModeVo(
    val ch: Int,
    private val _limitList: List<Int>
) {
    val limitList: List<String> by lazy { _limitList.map { it.toString() } }
}


data class MotionControlBase(
    val motionControlModeVoList: List<MotionControlModeVo>,
    val panel: JPanel? = null,
    val rtBtnAction: (JButton, List<MotionControlPanel>) -> FooterAction,
    val setBtnAction: (JButton, List<MotionControlPanel>) -> FooterAction
) : JPanel() {
    init {
        layout = GridBagLayout()
        val chArray = motionControlModeVoList.map { it.ch.toString() }.toTypedArray()

        val motionControlPanelList = mutableListOf<MotionControlPanel>()
        var motionControlPanel: MotionControlPanel
        motionControlModeVoList.forEachIndexed { index, vo ->
            motionControlPanel = MotionControlPanel(vo.ch.toString(), chArray, vo.limitList)
            motionControlPanelList.add(motionControlPanel)
            println("$motionControlPanel,${0},${index},0,${1}, ${1}")
            addItem(motionControlPanel, 0, index, 1, 1)
        }
        val panelFooter = MotionControlFooter(motionControlPanelList, rtBtnAction, setBtnAction)
//        addItem(panelFooter, 0, motionControlModeVoList.size, 1, 1)
//        panel?.let {
//            addItem(it, 1, 0, 1, motionControlModeVoList.size + 1)
//        } ?: addItem(JPanel(), 1, 0, 1, motionControlModeVoList.size + 1)

        val makeDummyPanel = { n: Int ->
            val dummyPanel1 = JPanel()
            dummyPanel1.layout = FlowLayout()
            dummyPanel1.add(JLabel("　"))
            dummyPanel1.setBounds(0, 0, 200, 200)
            addItem(dummyPanel1, 0, motionControlModeVoList.size - 1 + n, 1, 1)
        }
        val dummyPanelList = listOf(1, 2, 3, 4)
        dummyPanelList.forEach { makeDummyPanel(it) }

        addItem(panelFooter, 0, -1 + motionControlModeVoList.size + dummyPanelList.size + 1, 1, 1)
        panel?.let {
            addItem(it, 1, 0, 1, motionControlModeVoList.size + dummyPanelList.size + 1)
        } ?: addItem(JPanel(), 1, 0, 1, motionControlModeVoList.size + dummyPanelList.size + 1)

    }

    private fun addItem(panel: JPanel, x: Int, y: Int, w: Int, h: Int) {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = x
        gbc.gridy = y
        gbc.gridwidth = w
        gbc.gridheight = h
        (layout as GridBagLayout).setConstraints(panel, gbc)
        add(panel)
    }

}

data class MotionControlPanel(
    val ch: String,
    val chArray: Array<String>,
    val limitList: List<String>
) : JPanel() {
    private var cmbCh: JComboBox<String>

    //    val slider: JSlider = JSlider(JSlider.HORIZONTAL, -270, 270, 1)
    val slider: JSlider
    private val minusLimit: JTextField
    private val plusLimit: JTextField

    init {
//        layout =BorderLayout(5, 5)
        layout = BorderLayout()
        setBounds(0, 0, 200, 50)

        val centerWPanel = JPanel()
        centerWPanel.layout = GridLayout(1, 2)
        centerWPanel.add(JLabel("CH"))
        cmbCh = JComboBox(chArray)
        cmbCh.selectedItem = ch
        centerWPanel.add(cmbCh)

        slider = makeSlider(limitList[0].toInt(), limitList[1].toInt())
//        slider.majorTickSpacing = 90
//        slider.minorTickSpacing = 1
//        slider.paintTicks = true
//        slider.paintLabels = true

        val centerEPanel = JPanel()
        centerEPanel.layout = GridLayout(2, 2)
        centerEPanel.add(JLabel("-Limit"))

        minusLimit = JTextField(limitList[0])
        centerEPanel.add(minusLimit)
        centerEPanel.add(JLabel("+Limit"))
        plusLimit = JTextField(limitList[1])
        centerEPanel.add(plusLimit)

        add(centerWPanel, BorderLayout.WEST)
        add(slider, BorderLayout.CENTER)
        add(centerEPanel, BorderLayout.EAST)

    }

    private fun makeSlider(min: Int, max: Int): JSlider {
        val slider = JSlider(JSlider.HORIZONTAL, -270, 270, 1)
        slider.majorTickSpacing = 90
        slider.minorTickSpacing = 1
        slider.paintTicks = true
        slider.paintLabels = true
        (slider.labelTable as? Map<*, *>)?.forEach { (_, value) ->
            if (value is JLabel && value.text.toInt() !in min..max) {
                value.foreground = Color.RED
            }
        }
        slider.addMouseWheelListener { e ->
            (e.source as? JSlider)?.also {
                slider.value = it.value - e.wheelRotation * 5
            }
        }
        slider.model.addChangeListener { e ->
            (e.source as? BoundedRangeModel)?.also {
                it.value = it.value.coerceIn(min, max)
            }
        }
        return slider
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MotionControlPanel

        if (!chArray.contentEquals(other.chArray)) return false
        if (limitList != other.limitList) return false
        if (cmbCh != other.cmbCh) return false
        if (slider != other.slider) return false
        if (minusLimit != other.minusLimit) return false
        if (plusLimit != other.plusLimit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chArray.contentHashCode()
        result = 31 * result + limitList.hashCode()
        result = 31 * result + cmbCh.hashCode()
        result = 31 * result + slider.hashCode()
        result = 31 * result + minusLimit.hashCode()
        result = 31 * result + plusLimit.hashCode()
        return result
    }
}


data class MotionControlFooter(//
    private val motionControlPanel: List<MotionControlPanel>,//
    private val rtBtnAction: (JButton, List<MotionControlPanel>) -> FooterAction,
    private val setBtnAction: (JButton, List<MotionControlPanel>) -> FooterAction
) : JPanel() {
    private val rtBtn: JButton
    private val setBtn: JButton

    init {
        layout = FlowLayout()
        rtBtn = JButton("RT")
        setBtn = JButton("SET")
        rtBtn.addActionListener(rtBtnAction(rtBtn, motionControlPanel))
        setBtn.addActionListener(setBtnAction(setBtn, motionControlPanel))
        add(rtBtn)
        add(setBtn)
    }
}

abstract class FooterAction : ActionListener {
    abstract override fun actionPerformed(e: ActionEvent?)
}