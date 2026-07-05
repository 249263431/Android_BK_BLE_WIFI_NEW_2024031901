## <center>BK3435 BLE配网<center/>

1. APP发送数据格式
	
	|  协议头 | CMD  | 数据长度 | Data0 ....DataN|CRC|
	|  ----  | ---- | -------|------------| -----|
	
	*  协议头：2字节
		* APP向设备端发数据协议头：0x55 AA
	*  CMD命令位：1字节，表示操作类型
		* 0x01 APP连接成功后，发起配网请求			* 0x02 APP发送SSID
		* 0x03 APP发送Password  
	*  数据长度：表示 Data0...DataN 的长度
	*  Data0...DataN:表示实际数据
	*  CRC:表示CRC校验,协议头到DataN的CRC校验
	
2. 设备发送数据格式
	
	|  协议头 | CMD  | 数据长度 | Data0 ....DataN|CRC|
	|  ----  | ---- | -------|------------| -----|
	
	*  协议头：2字节
		* 设备端向APP发送数据协议头:0xAA 55
	*  CMD命令位：1字节，表示操作类型
		* 0x01   表示CRC校验不通过
		* 0x05 成功收到配网请求，APP下一步可以发SSID了 
		* 0x06 成功收到SSID,APP下一步可以发Password了 
		* 0x07 成功Password,设备开始连接路由器
	   * 0x08 连接路由器成功
	   * 0x09 连接路由器失败：连接超时
	   * 0xA0 连接路由器失败:SSID或密码错误
	*  数据长度：表示 Data0...DataN 的长度
	*  Data0...DataN:表示实际数据
	*  CRC:表示CRC校验,协议头到DataN的CRC校验

3. 配网流程
	1. 	APP发起连接请求
	2. APP发起配网请求
	3. 设备回复命令位：0x05
	4. APP发起SSID
	5. 设备回复命令位：0x06
	6. APP发起Password
	7. 设备回复命令位：0x07
	8. APP等待设备回复是否连接路由器成功
	

	