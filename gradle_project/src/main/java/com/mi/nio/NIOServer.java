package com.mi.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {
	
	private Selector selector;
	
	public void initServer(int port) throws IOException{
		ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(port));
		this.selector = Selector.open();
		//一般ServerSocketChannel只注册accept事件，对于read和write事件是注册到accept的SocketChannel中的
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	public void listen() throws IOException{
		System.out.println("server start up...");
		while(true){
			selector.select();
			
			Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
			while(iter.hasNext()){
				SelectionKey key = iter.next();
				//移除已经准备处理的事件
				iter.remove();
				
				if(key.isAcceptable()){
					ServerSocketChannel server = (ServerSocketChannel) key.channel();
					SocketChannel channel = server.accept();
					System.out.println("server accept");
					channel.configureBlocking(false);
					//注意  事件注册的对象不一样
					channel.register(selector, SelectionKey.OP_READ);
				}else if(key.isReadable()){
					read(key);
				}else if(key.isWritable()){
				}else if(key.isConnectable()){
				}
			}
		}
	}
	
	private void read(SelectionKey key) throws IOException {
		//开启一个线程处理结果
		SocketChannel channel = (SocketChannel)key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(10);
		int eof = channel.read(buffer);
		StringBuffer sbuffer = new StringBuffer();
		while(eof > 0){
			buffer.flip();
			while(buffer.hasRemaining()){
				sbuffer.append((char)buffer.get());
			}
			buffer.clear();
			eof = channel.read(buffer);
		}
		String msg = sbuffer.toString();
		System.out.println("server receive a msg :\n" + msg);
		
		//处理消息
		//sleep 1s
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		out.println("HTTP/1.1 200 OK");   
//	    out.println("Content-Type:text/html;charset:GBK");
		ByteBuffer outBuffer = ByteBuffer.wrap(("HTTP/1.1 200 OK").getBytes());
		channel.write(outBuffer);
	}

	public static void main(String[] args) throws IOException {
		NIOServer server = new NIOServer();  
        server.initServer(8000);  
        server.listen();
	}
}
