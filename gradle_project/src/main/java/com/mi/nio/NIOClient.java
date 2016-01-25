package com.mi.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient extends Thread {
	
	private Selector selector;
	private int no;
	
	public NIOClient(int no) {
		this.no = no;
	}
	
	public void initClient(String ip, int port) throws IOException{
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		this.selector = Selector.open();
		channel.connect(new InetSocketAddress(ip, port));
		channel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	public void listen() throws IOException{
		while(true){
			selector.select();
			
			Iterator<SelectionKey> iter = selector.keys().iterator();
			
			while(iter.hasNext()){
				SelectionKey key = iter.next();
				
				//连接
				if(key.isConnectable()){
					SocketChannel channel = (SocketChannel)key.channel();
					
					if(channel.isConnectionPending()){
						channel.finishConnect();
					}
					System.out.println("thread [" + this.no + "] connect to server success!");
					channel.register(selector, SelectionKey.OP_WRITE);
				}else if(key.isWritable()){
					write(key);
				}else if(key.isReadable()){
					read(key);
				}
			}
		}
	}

	
	private void write(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel)key.channel();
		channel.write(ByteBuffer.wrap(("thread [" + this.no + "]send message to server.").getBytes()));
		channel.register(selector, SelectionKey.OP_READ);
	}

	
	private void read(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int len = channel.read(buffer);
		byte[] data = buffer.array();
		String msg = new String(data).trim();
		System.out.println("client receive a msg :" + msg);
	}
	
	@Override
	public void run() {
		try {
			this.initClient("localhost", 8000);
			this.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
