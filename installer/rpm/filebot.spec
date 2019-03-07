Name:           @{package.name}
Version:        @{application.version}
Release:        universal
Summary:        @{package.synopsis}
License:        Proprietary

BuildArch:      noarch

Requires:       java-1.8.0-openjdk
Requires:       java-1.8.0-openjdk-openjfx
Requires:       jna
Requires:       mediainfo
Requires:       p7zip
Requires:       p7zip-plugins


%description
@{package.description}


%install
cp -rvf %{src}/usr %{buildroot}


%files
/*


%post
ln -sf /usr/share/filebot/bin/filebot.sh /usr/bin/filebot
ln -sf /usr/lib/java/jna.jar /usr/share/filebot/jar/jna.jar


%preun
rm -f /usr/bin/filebot
rm -f /usr/share/filebot/jar/jna.jar
