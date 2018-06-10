package net.filebot;

import static net.filebot.platform.windows.WinAppUtilities.*;

import java.io.File;

public enum LicenseModel {

	MicrosoftStore {

		@Override
		public void check() throws LicenseError {
			if (!getAppUserModelID().equals("PointPlanck.FileBot")) {
				throw new LicenseError("Invalid container state");
			}
		}
	},

	MacAppStore {

		@Override
		public void check() throws LicenseError {
			if (File.listRoots()[0].canRead()) {
				throw new LicenseError("Invalid container state");
			}
		}
	},

	PGPSignedMessage {

		@Override
		public void check() throws LicenseError {
			try {
				License.INSTANCE.get().check();
			} catch (Exception e) {
				throw new LicenseError(e.getMessage());
			}
		}
	};

	public abstract void check() throws LicenseError;

}
