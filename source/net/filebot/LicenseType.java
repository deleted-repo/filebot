package net.filebot;

import static net.filebot.Settings.*;
import static net.filebot.platform.windows.WinAppUtilities.*;
import static net.filebot.util.FileUtilities.*;

import java.io.File;

import net.filebot.util.SystemProperty;

public enum LicenseType {

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

		public final SystemProperty<File> LICENSE_FILE = SystemProperty.of("net.filebot.license", File::new, ApplicationFolder.AppData.resolve("license.txt"));
		public final Resource<License> LICENSE = Resource.lazy(() -> new License(readFile(LICENSE_FILE.get())));

		@Override
		public void check() throws LicenseError {
			try {
				License license = LICENSE.get();

				if (license.expired()) {
					throw new LicenseError("Expired: " + license);
				}
			} catch (Exception e) {
				throw new LicenseError(e.getMessage());
			}
		}
	};

	public abstract void check() throws LicenseError;

	public static LicenseType get() {
		if (isUWP())
			return MicrosoftStore;
		if (isMacSandbox())
			return MacAppStore;

		return PGPSignedMessage;
	}

}
