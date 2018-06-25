package com.feeyo.redis.net.backend;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feeyo.net.nio.NIOHandler;
import com.feeyo.net.nio.NetFlowController;
import com.feeyo.net.nio.util.StringUtil;
import com.feeyo.redis.net.front.RedisFrontConnection;

/**
 * backend redis NIO handler (only one for all backend redis connections)
 * 
 * @author zhuam
 *
 */
public class RedisBackendConnectionHandler implements NIOHandler<RedisBackendConnection> {
	
	private static Logger LOGGER = LoggerFactory.getLogger( RedisBackendConnectionHandler.class );
	
	@Override
	public void onConnected(RedisBackendConnection con) throws IOException {	
        // 已经连接成功
		con.getCallback().connectionAcquired( con );		
	}
	
	@Override
	public void handleReadEvent(RedisBackendConnection con, byte[] data) throws IOException {
		
		// 日志HEX
		if ( LOGGER.isDebugEnabled() ) {
			final String hexs = StringUtil.dumpAsHex(data, 0, data.length);
			LOGGER.debug("C#{} backend response len = {},  buffer bytes\n {}", 
					new Object[]{ con.getId(), data.length, hexs });
		}
		
		
		// 回调
		con.getCallback().handleResponse(con, data);	
		return;	
	}	

	@Override
	public void onClosed(RedisBackendConnection con, String reason) {
		if ( con.getCallback() != null )
			con.getCallback().connectionClose(con, reason);
	}

	@Override
	public void onConnectFailed(RedisBackendConnection con, Exception e) {
		if ( con.getCallback() != null )
			con.getCallback().connectionError(e, con);		
	}

	@Override
	public boolean handleNetFlow(RedisBackendConnection con, int dataLength) throws IOException {
		if (con.getAttachement() instanceof RedisFrontConnection) {
			RedisFrontConnection rfc = (RedisFrontConnection) con.getAttachement();
			NetFlowController nfm = con.getNetflowController();
			if (nfm != null)
				return nfm.consumeBytes(rfc.getPassword(), dataLength);
		}
		return false;
	}

}
