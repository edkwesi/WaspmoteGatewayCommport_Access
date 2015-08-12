/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package port_access;

/**
 *
 * @author kwesi
 */
import java.io.*;
import java.util.*;
import javax.comm.*;
import java.net.*;

public class Port_Access implements Runnable, SerialPortEventListener {

    static CommPortIdentifier portId;
    static Enumeration portList;
    InputStream inputStream;
    SerialPort serialPort;
    Thread readThread;
    String data, node, reading, battery;
    char character;

    //Establish connection to server
    public void connect(String url) throws IOException {
        URL client = new URL(url);
        URLConnection url_connect = client.openConnection();
        InputStream stream = url_connect.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader in = new BufferedReader(reader);
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println(inputLine);
        }
        in.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic hered
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                //selecting com port identifier for gateway
                if (portId.getName().equals("COM10")) {
                    //for linux(portId.getName().equals("/dev/term/a")) {
                    Port_Access reader = new Port_Access();
                }
            }
        }
    }

    public Port_Access() {
        try {
            serialPort = (SerialPort) portId.open("Port Access App", 2000);
        } catch (PortInUseException e) {
            System.out.println(e);
        }
        try {
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            System.out.println(e);
        }
        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            System.out.println(e);
        }
        serialPort.notifyOnDataAvailable(true);
        try {
            // setting serial port parameters
            serialPort.setSerialPortParams(9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
            System.out.println(e);
        }
        readThread = new Thread(this);
        readThread.start();
    }

    //threading port acccess
    public void run() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public void serialEvent(SerialPortEvent event) throws IOException {
        //String url_param_1,url_param_2;
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                byte[] readBuffer = new byte[50];
                try {
                    while (inputStream.available() > 0) 
                    {
                        int numBytes = inputStream.read();
                        if (numBytes != 10) 
                        {
                            data += (char) numBytes + "";
                        } 
                        else 
                        {
                            for (int i = 0; i < data.length(); i++)
                            {
                                character = data.charAt(i);

                                if (character == 'n')
                                    node = data.substring(i + 5, i + 8);
                                
                                if (character == 'v') 
                                    reading = data.substring(i + 6, i + 14);
                                
                                if (character == 'b')
                                    battery = data.substring(i + 8, i + 10); 
                            }
                            System.out.println(node+" "+reading+" "+battery);
                            data = "";
                            //adding data to url url params for CMS
                            //url_param_1="http://localhost:49422/waterinput/post_water?nodeId="+node+"&csv="+reading;
                            //url_param_2="http://localhost:49422/node/create?battery="+battery;
                            //sending values to CMS
                            //connect(url_param_1);
                            //connect(url_param_2);
                        }
                    }
                } catch (IOException e) {
                    System.out.println(e);
                }
                break;
        }
    }
}