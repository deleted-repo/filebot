package net.filebot.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WTypes.LPWSTR;
import com.sun.jna.platform.win32.WinDef.UINTByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends StdCallLibrary {

	Kernel32 INSTANCE = Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

	long GetCurrentPackageFullName(UINTByReference packageFullNameLength, LPWSTR packageFullName);

	long APPMODEL_ERROR_NO_PACKAGE = 15700;

}
