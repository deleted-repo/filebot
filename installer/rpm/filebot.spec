Name:           @{package.name}
Version:        @{application.version}
Release:        universal
Summary:        @{package.synopsis}

BuildArch:      noarch

Requires:       java-1.8.0-openjdk
Requires:       java-1.8.0-openjdk-openjfx
Requires:       jna
Requires:       libmediainfo
Requires:       libchromaprint
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


%postun
rm -f /usr/bin/filebot

