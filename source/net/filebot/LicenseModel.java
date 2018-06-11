package net.filebot;

import static net.filebot.platform.windows.WinAppUtilities.*;

import java.io.File;

public enum LicenseModel {

	MicrosoftStore {

		private final Resource<Boolean> CHECK = Resource.lazy(() -> !getAppUserModelID().equals("PointPlanck.FileBot"));

		@Override
		public void check() throws LicenseError {
			try {
				if (CHECK.get()) {
					throw new LicenseError("Microsoft Store: Desktop Bridge not found");
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	},

	MacAppStore {

		private final Resource<Boolean> CHECK = Resource.lazy(() -> !File.listRoots()[0].canRead());

		@Override
		public void check() throws LicenseError {
			try {
				if (CHECK.get()) {
					throw new LicenseError("Microsoft Store: Desktop Bridge not found");
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
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

	public boolean isFile() {
		return this == PGPSignedMessage;
	}

	public abstract void check() throws LicenseError;

}
