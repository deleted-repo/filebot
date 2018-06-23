package net.filebot;

import static net.filebot.platform.windows.WinAppUtilities.*;

import java.io.File;

public enum LicenseModel {

	MicrosoftStore {

		private final Resource<Boolean> AUMID = Resource.lazy(() -> getAppUserModelID().equals("PointPlanck.FileBot"));

		@Override
		public Object check() throws LicenseError {
			try {
				if (!AUMID.get()) {
					throw new LicenseError("Desktop Bridge not found");
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			return "Microsoft Store License";
		}
	},

	MacAppStore {

		private final Resource<Boolean> SANDBOX = Resource.lazy(() -> !File.listRoots()[0].canRead());

		@Override
		public Object check() throws LicenseError {
			try {
				if (!SANDBOX.get()) {
					throw new LicenseError("Mac App Sandbox not found");
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			return "Mac App Store License";
		}
	},

	PGPSignedMessage {

		@Override
		public License check() throws LicenseError {
			try {
				return License.INSTANCE.get().check();
			} catch (Exception e) {
				throw new LicenseError(e.getMessage());
			}
		}
	};

	public boolean isFile() {
		return this == PGPSignedMessage;
	}

	public abstract Object check() throws LicenseError;

}
