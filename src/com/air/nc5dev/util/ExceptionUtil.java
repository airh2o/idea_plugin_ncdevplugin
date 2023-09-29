package com.air.nc5dev.util;

import com.air.nc5dev.exception.BusinessException;

/**
 * 异常工具类
 *                       </br>
 *                       </br>
 * @author air Email:209308343@qq.com
 * @version NC505+,JDK1.5
 * @date 2019-9-9 下午9:54:53
 */
public  final class ExceptionUtil extends cn.hutool.core.exceptions.ExceptionUtil{
	public static Throwable getTopCase(Throwable e){
		if (e == null) {
			return null;
		}

		Throwable e1 = e.getCause();

		while(e1 != null && e1 != e){
			e1 = e1.getCause();
			if (e1 != null) {
				e = e1;
			}
		}

		return e1 == null ? e : e1;
	}

	/**
	 * 获取所有错误栈代码方法全限定名字和行数
	 * 
	 * @param e
	 * @return
	 */
	public static String getExcptionStackTraceDetall(Throwable e) {
		StringBuilder sb = new StringBuilder(2000);
		StackTraceElement[] stackTrace = e.getStackTrace();
		
		sb.append("\n 异常栈如下: \n");
		
		int i = 0;
		for (StackTraceElement s : stackTrace) {
			sb.append("Stack-").append(i++);
			sb.append('=').append(s.getClassName()).append('.')
				.append(s.getMethodName()).append('(').append(')')
				.append(" 方法第 ").append(s.getLineNumber()).append(" 行 \n ");
		}

		return sb.toString();
	}
	
	/**
	 * 	获得异常的简短描述+所有错误栈代码方法全限定名字和行数
	 * 
	 * @param e
	 * @return
	 */
	public static String getExcptionDetall(Throwable e) {
		StringBuilder sb = new StringBuilder(3000);
		 
		sb.append("方法处理异常! 异常原因：" ).append(e.toString())
			.append(getExcptionStackTraceDetall(e));

		return sb.toString();
	}
	/**
	 * 获得异常的简短描述+最近3行的代码方法全限定名字和行数
	 *
	 * @author air
	 * @date 2019年8月6日 下午1:58:56
	 * @param e
	 * @return
	 */
	public static String toString3Lines(Throwable e) {
		return toStringLines(e, 3);
	}
	/**
	 * 获得异常的简短描述+最近1行的代码方法全限定名字和行数
	 *
	 * @author air
	 * @date 2019年8月6日 下午1:58:56
	 * @param e
	 * @return
	 */
	public static String toString1Lines(Throwable e) {
		return toStringLines(e, 1);
	}
	/**
	 * 获得异常的简短描述+最近2行的代码方法全限定名字和行数
	 *
	 * @author air
	 * @date 2019年8月6日 下午1:58:56
	 * @param e
	 * @return
	 */
	public static String toString2Lines(Throwable e) {
		return toStringLines(e, 2);
	}
	/**
	 * 获得异常的简短描述+最近 lines 行的代码方法全限定名字和行数
	 *
	 * @author air
	 * @date 2019年8月6日 下午1:58:56
	 * @param e
	 * @param lines 需要获得多少层异常栈信息
	 * @return
	 */
	public static String toStringLines(Throwable e , int lines) {
		StringBuilder sb = new StringBuilder(3000);
		sb.append(e.toString());
		
		StackTraceElement[] stackTrace = e.getStackTrace();
		
		if(null != stackTrace  ) {
			int lineSize = stackTrace.length < lines ? stackTrace.length : lines;
			for (int i = 0; i < lineSize; i++) {
				StackTraceElement s = stackTrace[i];
				sb.append(" on ")
					.append(s.getClassName()).append('.')
					.append(s.getMethodName()).append('(').append(')')
					.append(" line ").append(s.getLineNumber()).append("\n");
			}
		}
		
		return sb.toString();
	}
	/**
	 * 调用此方法 抛出一个指定错误消息的 异常
	 *                       </br>
	 *                       </br>
	 * @author air Email:209308343@qq.com 
	 * @date 2019-10-26 下午7:18:31
	 * @param errorMsg 异常消息
	 * @throws BusinessException
	 */
	public static final void makeException(String errorMsg) throws BusinessException {
		throw new BusinessException(errorMsg);
	}
	/**
	 * 调用此方法 把一个 Throwable 抛出成  BusinessException 异常
	 *                       </br>
	 *                       </br>
	 * @author air Email:209308343@qq.com 
	 * @date 2019-10-26 下午7:18:31
	 * @param error 异常消息
	 * @throws BusinessException
	 */
	public static final void makeException(Throwable error) throws BusinessException {
		BusinessException be = null;
		if(error instanceof BusinessException){
			be = (BusinessException) error;
		}else{
			be = new BusinessException(error.toString());
			be.setStackTrace(error.getStackTrace());
		}
		throw be;
	}
	/**
	 * 调用此方法 把一个 Throwable 抛出成  BusinessException 异常,且消息用errorMsg替换,但错误栈保留
	 *                       </br>
	 *                       </br>
	 * @author air Email:209308343@qq.com 
	 * @date 2019-10-26 下午7:18:31
	 * @param errorMsg 异常消息
	 * @throws BusinessException
	 */
	public static final void makeException(Throwable error,String errorMsg) throws BusinessException {
		BusinessException be = new BusinessException(errorMsg);
		be.setStackTrace(error.getStackTrace()); 
		throw be;
	}
}
