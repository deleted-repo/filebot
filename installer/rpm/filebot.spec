Name:           @{package.name}
Version:        @{application.version}
Release:        universal.jdk8
Summary:        @{package.synopsis}
License:        Proprietary

BuildArch:      noarch

Recommends:     java-1.8.0-openjdk
Recommends:     java-1.8.0-openjdk-openjfx
Recommends:     jna
Recommends:     mediainfo
Recommends:     p7zip
Recommends:     p7zip-plugins


%description
@{package.description}


%install
cp -rvf %{src}/usr %{src}/etc %{buildroot}


%files
/*


%post
ln -sf /usr/share/filebot/bin/filebot.sh /usr/bin/filebot
ln -sf /usr/lib/java/jna.jar /usr/share/filebot/jar/jna.jar


%preun
rm -f /usr/bin/filebot
rm -f /usr/share/filebot/jar/jna.jar
