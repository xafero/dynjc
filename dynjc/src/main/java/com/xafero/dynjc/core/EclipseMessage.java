package com.xafero.dynjc.core;

import java.awt.TrayIcon.MessageType;
import java.io.File;

public class EclipseMessage {

	public final File source;
	public final int id;
	public final MessageType type;
	public final int line;
	public final String place;
	public final String message;

	public EclipseMessage(File source, int id, MessageType type, int line, String place, String message) {
		this.source = source;
		this.id = id;
		this.type = type;
		this.line = line;
		this.place = place;
		this.message = message;
	}

	@Override
	public String toString() {
		return "[source=" + source + ", id=" + id + ", type=" + type + ", line=" + line + ", place=" + place
				+ ", message=" + message + "]";
	}
}