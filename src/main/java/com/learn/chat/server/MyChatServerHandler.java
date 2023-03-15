package com.learn.chat.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MyChatServerHandler extends SimpleChannelInboundHandler<String> {
    // 定义存储各个客户端连接的group
    // GlobalEventExecutor 伸缩性并不好
    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 实现广播，通知
        // 需要服务器端保存好各个客户端的连接
        Channel channel = ctx.channel();
        // 广播内容
        // channelGroup.writeAndFlush :将消息写入所有连接的channel
        channelGroup.writeAndFlush("[客户端] - " + channel.remoteAddress() + " 加入 [服务器] \n");
        channelGroup.add(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端: " + channel.remoteAddress() + " 上线了");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 聊天实时场景
        // abc 三个客户分别与服务器连接，a先连接，
        // b连接，告诉a已经上线
        // c连接，告诉ab已经上线
        Channel channel = ctx.channel();
        channelGroup.forEach(client -> {
            if (client != channel) {
                client.writeAndFlush(" 客户端: " + channel.remoteAddress() + " 发送消息：" + msg + "\n");
            } else {
                channel.writeAndFlush(" 客户端自己: " + msg + "\n");
            }
        });


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("客户端: " + channel.remoteAddress() + " 下线了");
    }

    /**
     * 断掉连接
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        // 广播内容
        // channelGroup.writeAndFlush :将消息写入所有连接的channel
        channelGroup.writeAndFlush("[客户端] - " + channel.remoteAddress() + " 离开 [服务器] \n");
        channelGroup.remove(channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
