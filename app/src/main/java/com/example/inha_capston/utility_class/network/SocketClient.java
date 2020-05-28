package com.example.inha_capston.utility_class.network;

import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketClient
{
    public SocketClient() {
        Log.i("df","before try");
        Socket socket = null ;
        InputStream is = null ;
        OutputStream os = null ;
        Log.i("df","before try");
        try {
            // 소켓 생성 및 연결.
            Log.i("df","inside try");
            socket = new Socket() ;
            Log.i("socket","say");
            SocketAddress addr = new InetSocketAddress("172.28.0.2", 1234/*port*/) ;
            Log.i("socket","say");
            socket.connect(addr) ;
            Log.i("socket","say");
            // 데이터 수신.
            byte[] bufRcv = new byte[1024] ;
            int size ;

            is = socket.getInputStream() ;
            size = is.read(bufRcv) ;

            // TODO : process bufRcv.

            // 데이터 송신.
            byte[] bufSnd = new byte[3] ;

            // TODO : fill bufSnd with data.
            bufSnd[0] = 0x00 ;
            bufSnd[1] = 0x11;
            bufSnd[2] =0;

            os = socket.getOutputStream() ;
            os.write(bufSnd, 0/*off*/, 16/*len*/) ;
        } catch (Exception e) {
            e.printStackTrace();
            // TODO : process exceptions.
        }

        try {
            // 소켓 종료.
            if (is != null)
                is.close() ;

            if (os != null)
                os.close() ;

            if (socket != null)
                socket.close() ;
        } catch (Exception e) {
            // TODO : process exceptions.
        }
    }
}
