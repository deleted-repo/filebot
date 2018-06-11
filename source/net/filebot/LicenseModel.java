package net.filebot;

import static net.filebot.platform.windows.WinAppUtilities.*;

import java.io.File;

public enum LicenseModel {

	MicrosoftStore {

		@Override
		public boolean isAppStore() {
			return true;
		}

		@Override
		public void check() throws LicenseError {
			if (!getAppUserModelID().equals("PointPlanck.FileBot")) {
				throw new LicenseError("Microsoft Store: Desktop Bridge not found");
			}
		}
	},

	MacAppStore {

		@Override
		public boolean isAppStore() {
			return true;
		}

		@Override
		public void check() throws LicenseError {
			if (File.listRoots()[0].canRead()) {
				throw new LicenseError("Mac App Store: App Sandbox not found");
			}
		}
	},

	PGPSignedMessage {

		@Override
		public boolean isAppStore() {
			return false;
		}

		@Override
		public void check() throws LicenseError {
			try {
				License.INSTANCE.get().check();
			} catch (Exception e) {
				throw new LicenseError(e.getMessage());
			}
		}
	};

	public abstract boolean isAppStore();

	public abstract void check() throws LicenseError;

}
