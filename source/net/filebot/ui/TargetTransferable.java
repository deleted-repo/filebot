package net.filebot.ui;

import java.awt.datatransfer.Transferable;

public class TargetTransferable {

	private final PanelBuilder target;
	private final Transferable transferable;

	public TargetTransferable(PanelBuilder target, Transferable transferable) {
		this.target = target;
		this.transferable = transferable;
	}

	public PanelBuilder getTarget() {
		return target;
	}

	public Transferable getTransferable() {
		return transferable;
	}

}
