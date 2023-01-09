import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import kotlinx.coroutines.*
import ui.*
import util.ServoControllerUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.JFrame.EXIT_ON_CLOSE


class ServoControllerMain() : JFrame() {



    init {
        val panelBase = JPanel()
        val text1 = JTextField()
        val text2 = JTextField(20)
        val text3 = JTextField("テキスト３")
        val text4 = JTextField("テキスト４", 20)
        panelBase.add(text1)
        panelBase.add(text2)
        panelBase.add(text3)
        panelBase.add(text4)
        contentPane.add(panelBase)
    }

}

class COMM() : JFrame() {
    private var cmbCom: JComboBox<String> = JComboBox(ServoControllerUtil.getSerialPortNames())
    private val btnConnect: JButton = JButton("接続")
    private val txt1: JTextArea
    private val txt2: JTextArea
    private val btnSend: JButton

    private var cmbCh: JComboBox<String> = JComboBox(arrayOf("1", "2"))
    private val slider1: JSlider = JSlider(JSlider.HORIZONTAL, -270, 270, 1)
    private val minusLimit: JTextField = JTextField("-200")
    private val plusLimit: JTextField = JTextField("+200")

    init {
        btnConnect.addActionListener(BtnConnectListener())

        val northPanel = JPanel()
        northPanel.layout = BoxLayout(northPanel, BoxLayout.LINE_AXIS)
        northPanel.add(cmbCom)
        northPanel.add(btnConnect)


        txt1 = JTextArea("")
//        txt1.preferredSize
        val spane = JScrollPane(txt1)
        spane.preferredSize = Dimension(200, 150)


//        val centerPanel = JPanel()
//        centerPanel.layout = BorderLayout(5,5)

//        val centerWPanel = JPanel()
////        centerWPanel.layout = FlowLayout()
//        centerWPanel.layout = GridLayout(1,2)
//        centerWPanel.add(JLabel("CH"))
//        centerWPanel.add(cmbCh)
//
//        slider1.majorTickSpacing = 90
//        slider1.minorTickSpacing = 1
//        slider1.paintTicks = true
//        slider1.paintLabels = true
//
//        val centerEPanel = JPanel()
//        centerEPanel.layout = GridLayout(2,2)
//        centerEPanel.add(JLabel("-Limit"))
//        centerEPanel.add(minusLimit)
//        centerEPanel.add(JLabel("+Limit"))
//        centerEPanel.add(plusLimit)
//
//        centerPanel.add(centerWPanel,BorderLayout.WEST)
//        centerPanel.add(slider1,BorderLayout.CENTER)
//        centerPanel.add(centerEPanel, BorderLayout.EAST)

        val motionControlModeVo = listOf(
            MotionControlModeVo(8, listOf(-200, 200)),
            MotionControlModeVo(9, listOf(-200, 200)),
            MotionControlModeVo(1, listOf(-200, 200)),
            MotionControlModeVo(2, listOf(-200, 200))
        )

        val rtBtnAction = { item: JButton, list: List<MotionControlPanel> -> RtBtnAction(item, list) }
        val setBtnAction = { item: JButton, list: List<MotionControlPanel> -> SetBtnAction(item, list) }
        val motionEditor = MotionEditorBase()
        val centerPanel = MotionControlBase(
            motionControlModeVo,
            motionEditor,//ServoControllerMain().contentPane as JPanel,
            rtBtnAction,
            setBtnAction
        )


        val southPanel = JPanel()
        southPanel.layout = BoxLayout(southPanel, BoxLayout.Y_AXIS)
        southPanel.add(spane)
        southPanel.add(JSeparator(JSeparator.HORIZONTAL))


        val southPanelSouth = JPanel()
        southPanelSouth.layout = BorderLayout()

        txt2 = JTextArea("")
        txt2.preferredSize = Dimension(200, 40)
        btnSend = JButton("送信")

        btnSend.addActionListener(BtnSendListener())
        southPanelSouth.add(txt2, BorderLayout.CENTER)
        southPanelSouth.add(btnSend, BorderLayout.EAST)
        southPanel.add(southPanelSouth)


        layout = BorderLayout(5, 5)
        add(northPanel, BorderLayout.NORTH)
//        add(btnSend, BorderLayout.EAST)
//        add(txt1, BorderLayout.CENTER)
//        add(txt2, BorderLayout.SOUTH)
        add(centerPanel, BorderLayout.CENTER)
        add(southPanel, BorderLayout.SOUTH)

        ServoControllerUtil.addListener(SerialReadListener())
    }

    private fun transmitData(sStr: String) {
        ServoControllerUtil.sendString(sStr)
        txt1.append("-->$sStr\r\n")
        appendAfterProcess()

    }

    private fun motionSend(list: List<MotionControlPanel>) {
        list.forEach {
            val sStr = "S0 ${it.ch} ${it.slider.value}"
            transmitData(sStr)
        }
    }

    inner class RtBtnAction(private val btn: JButton, private val list: List<MotionControlPanel>) : FooterAction() {
        var toggle = false
        lateinit var job: Job

        override fun actionPerformed(e: ActionEvent?) {
            toggle = !toggle
            btn.text = if (toggle) {
                job =
                    GlobalScope.launch(Dispatchers.Main) {
                        async {
                            while (true) {
                                motionSend(list)
                                delay(100)
                            }
                        }
                    }
                "rtNow"
            } else {
                job.cancel()
                "rt"
            }
        }
    }

    inner class SetBtnAction(private val btn: JButton, private val list: List<MotionControlPanel>) : FooterAction() {
        override fun actionPerformed(e: ActionEvent?) {
            motionSend(list)
        }
    }

    //自動的にスクロールさせる
    private fun appendAfterProcess() {
        val doc = txt1.document
//        doc.insertString(doc.length, text + \n, null)
        txt1.caretPosition = doc.length
    }

    inner class BtnSendListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val sStr: String = txt2.text
            if (sStr != "") {
                transmitData(sStr)
                txt2.text = ""
            }
        }
    }

    inner class SerialReadListener() : SerialPortDataListener {
        override fun getListeningEvents(): Int {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED
        }

        override fun serialEvent(event: SerialPortEvent?) {
            val b = event?.receivedData ?: throw java.lang.RuntimeException()
            txt1.append("<--${String(b)}")
            appendAfterProcess()
        }

    }

    inner class BtnConnectListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            val item = cmbCom.selectedItem as String
            ServoControllerUtil.getSerialPort(item, ServoControllerUtil.baudRateOptions[6])
        }
    }

}

fun main(args: Array<String>) {
//    val frame = ServoControllerMain()
    EventQueue.invokeLater {
        val frame = COMM()
        frame.apply {
            defaultCloseOperation = EXIT_ON_CLOSE
            title = "JTextField サンプル01"
            //setBounds(100, 200, 400, 150)
            setBounds(100, 200, 800, 600)
            isVisible = true
        }
    }
}