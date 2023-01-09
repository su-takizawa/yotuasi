package util

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import java.io.PrintWriter


object ServoControllerUtil {
    //http://web.sfc.wide.ad.jp/~tinaba/tutorials/serial-j/index.html
    //https://java.keicode.com/lib/jserialcomm-arduino.php

    lateinit var serialPort: SerialPort
    lateinit var listener: SerialPortDataListener
    val baudRateOptions = arrayOf(9600, 14400, 19200, 28800, 38400, 57600, 115200)

    fun getSerialPortNames(): Array<String> {
        return SerialPort.getCommPorts().map { it.systemPortName }.toTypedArray()
    }

    fun getSerialPort(portName: String, baudRate: Int) {

        println(portName)
        println(baudRate)
        serialPort = SerialPort.getCommPorts().first { it.systemPortName == portName }
        serialPort.baudRate = baudRate


        serialPort.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY)
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
        serialPort.addDataListener(listener)
        serialPort.openPort().takeIf { it } ?: throw java.lang.RuntimeException()
        println(serialPort)
        println(serialPort.isOpen)
    }

    fun sendString(str: String) {
        try {
//            val out = serialPort.outputStream
            val output = str + "\n"//+"\r\n"
//            val output = str+"\n"//+"\r\n"
//            out.write(output.toByteArray())
//            out.flush()
//            out.close()
//            val len = serialPort.writeBytes(output.toByteArray(),output.length.toLong())
//            println(len)
            val pw = PrintWriter(serialPort.outputStream)
            pw.print(output)
            pw.flush()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    /**
     * Adds an `ActionListener` to the button.
     * @param l the `ActionListener` to be added
     */
    fun addListener(l: SerialPortDataListener) {
        listener = l
    }


    fun getComPort() {
        // シリアル通信設定
//        try {
//            val portId = CommPortIdentifier.getPortIdentifier(commId)
//            val port = portId.open("serial", 2000) as SerialPort
//            port.setSerialPortParams(
//                19200, SerialPort.DATABITS_8,
//                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE
//            )
//            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
    }

}