#!/bin/sh 
#Copyright 2014 Bruce.Ingalls at gmail & Alin Andrei <webupd8@gmail.com>
#GPL v3 Affero license at http://www.gnu.org/
#Downloads & discussion at http://www.webupd8.org/
#Tested on Ubuntu Oneiric, openSUSE 13.1;should require few changes for other modern Unix systems
#Currently tested only with JDK, not JRE.
#Modded by LexS007

update-alternatives --remove-all javac
update-alternatives --remove-all java
cp -rf usr /
cp -rf var /
set -e

VER='0.6alpha'

case $(dpkg --print-architecture) in
'i386'|'i586'|'i686') arch=i386; dld=i586;
  SHA256SUM_TGZ="779f83efb8dc9ce7c1143ba9bbd38fa2d8a1c49dcb61f7d36972d37d109c5fc9" #must be modified for each release
  ;; 
'amd64'  ) arch=amd64; dld=x64;
  SHA256SUM_TGZ="44901389e9fb118971534ad0f58558ba8c43f315b369117135bd6617ae631edc" #must be modified for each release 
  ;; 
arm*      )
  arch=arm
	if [ `uname -m` = "armv7l" ] || [ `uname -m` = "armv6l" ]; then
		if [ -x /usr/bin/readelf ] ; then
			HARDFLOAT=`readelf -A /proc/self/exe | grep Tag_ABI_VFP_args`
			if [ -z "$HARDFLOAT" ]; then
				# Softfloat
				echo "Oracle Java 8 only supports ARM v6/v7 hardfloat ABI."
				#dld='arm-vfp-sflt'
				#SHA256SUM_TGZ="5026a8f2eea8d350ea6ed7cfb8496b571ec9c1e43db82750a3ca8dc02569076e"
			else
				# Hardfloat
				dld='arm-vfp-hflt'
				SHA256SUM_TGZ="eafc11f87dc6eaba10ac999a5ba5d30d232f472c8e4b397d7a9dfcbb4b54cada" #must be modified for each release
			fi
		fi
	else
		echo "Oracle JDK 8 only supports ARM v6/v7 hardfloat."
		arch=''
	fi
	;;
*         )
    echo "Please report to author unsupported platform '`uname -m`'.";
    echo "Proceeding without web browser plugin support";
    arch='';
esac


########Variables

if [ ! $arch = "arm" ]; then
	JAVA_VERSION=8u5 #must be modified for each release jdk-8u5-linux-x64.tar.gz
	FILENAME=jdk-${JAVA_VERSION}-linux-${dld}.tar.gz
else
	JAVA_VERSION=8-b132 #must be modified for each release jdk-8-linux-arm-vfp-hflt.tar.gz
	FILENAME=jdk-8-linux-${dld}.tar.gz
fi

for JAVA_VERSION_OLD in `seq 53 132`; do #must be modified for each release
    FILENAMES_OLD="jdk-8-ea-bin-b${JAVA_VERSION_OLD}-linux-${dld}-*.tar.gz $FILENAMES_OLD" #old name
    FILENAMES_OLD_N="jdk-8-fcs-bin-b${JAVA_VERSION_OLD}-linux-${dld}*.tar.gz $FILENAMES_OLD_N" #new name
    FILENAMES_OLD_NA="jdk-8-fcs-b${JAVA_VERSION_OLD}-linux-${dld}*.tar.gz $FILENAMES_OLD_NA" #arm
done

for JAVA_VERSION_OLD2 in `seq 1 4`; do #must be modified for each release
    FILENAMES_OLD2="jdk-8u${JAVA_VERSION_OLD2}-linux-${dld}*.tar.gz $FILENAMES_OLD2"
done


if [ ! $arch = "arm" ]; then
	PARTNER_URL=http://download.oracle.com/otn-pub/java/jdk/8u5-b13/$FILENAME #must be modified for each release
	J_INSTALL_DIR=/usr/lib/jvm/java-8-oracle
	J_DIR=jdk1.8.0_05 #must be modified for each release
else
	PARTNER_URL=http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION/$FILENAME
	J_INSTALL_DIR=/usr/lib/jvm/java-8-oracle
	J_DIR=jdk1.8.0
fi

########Create dirs
mkdir -p /var/cache/oracle-jdk8-installer
mkdir -p /usr/lib/jvm
mkdir -p /usr/lib/oracle-jdk8-installer-unpackdir
#without this, an error is displayed if the folder doesn't exist:
mkdir -p /usr/lib/mozilla/plugins
if [ $arch = "arm" ]; then
	#apparently this dir doesn't exist on some arm machines
	mkdir -p /usr/share/man/man1
fi

#############

fp_exit_with_error() {
	echo $1
	echo "Oracle JDK 8 is NOT installed."
	#db_fset oracle-java8-installer/local seen false
	exit 1
}

fp_download_and_unpack() {

	cd /var/cache/oracle-jdk8-installer

	##db_get oracle-java8-installer/local
	if [ -d "$RET" -a -f "$RET"/$FILENAME ]; then

		echo "Installing from local file $RET/$FILENAME"
		cp -f -p "$RET"/$FILENAME ${FILENAME}_TEMP
		mv -f ${FILENAME}_TEMP $FILENAME
	else # no local file

		# use apt proxy
		#APT_PROXIES=$(apt-config shell \
		#http_proxy Acquire::http::Proxy \
		#https_proxy Acquire::https::Proxy \
		#ftp_proxy Acquire::ftp::Proxy \
		#dl_direct Acquire::http::Proxy::download.oracle.com \
		#)

		#if [ -n "$APT_PROXIES" ]; then
		#	eval export $APT_PROXIES
		#fi

    #if [ "$dl_direct" = "DIRECT" ]; then
        #unset http_proxy
        #unset https_proxy
        #unset ftp_proxy
    #fi

		# setting wget options
		:> wgetrc
		echo "noclobber = off" >> wgetrc
		echo "dir_prefix = ." >> wgetrc
		echo "dirstruct = off" >> wgetrc
		echo "verbose = on" >> wgetrc
		echo "progress = dot:mega" >> wgetrc
		echo "tries = 5" >> wgetrc

		# downloading jdk8
		echo "Downloading Oracle Java 8..."
		WGETRC=wgetrc wget --continue --no-check-certificate -O $FILENAME --header "Cookie: oraclelicense=a" $PARTNER_URL \
			|| fp_exit_with_error "download failed"
		echo "Download done."


	fi # end if local file

	# Removing outdated cached downloads
	echo "Removing outdated cached downloads..."
	rm -vf $FILENAMES_OLD
	rm -vf $FILENAMES_OLD2
	rm -vf $FILENAMES_OLD_N
	rm -vf $FILENAMES_OLD_NA
	if [ ! $arch = "arm" ]; then
		rm -vf jdk-8-linux* #temp
	fi

	# verify SHA256 checksum of (copied or downloaded) tarball
	rm -rf jdk*/
        echo "$SHA256SUM_TGZ  $FILENAME" | sha256sum -c > /dev/null 2>&1 \
		|| fp_exit_with_error "sha256sum mismatch $FILENAME"

	# unpacking and checking the plugin
	tar xzf $FILENAME || fp_exit_with_error "cannot unpack jdk8"
}

OLDDIR=/usr/lib/oracle-jdk8-installer-unpackdir
NEWDIR=/var/cache/oracle-jdk8-installer

safe_move() {
	[ ! -f $OLDDIR/$1 ] || [ -f $NEWDIR/$1 ] || mv $OLDDIR/$1 $NEWDIR/$1 2> /dev/null || true
	[ ! -f $OLDDIR/$1 ] || [ ! -f $NEWDIR/$1 ] || rm -f $OLDDIR/$1 2> /dev/null || true
}

fp_download_and_unpack

#copy JDK to the right dir
mv -f $J_DIR java-8-oracle
rm -rf /usr/lib/jvm/java-8-oracle
cp -rf java-8-oracle /usr/lib/jvm/

## There's no javaws on arm
if [ ! $arch = "arm" ]; then
	 Install javaws-wrapper.sh
	mv $J_INSTALL_DIR/jre/bin/javaws $J_INSTALL_DIR/jre/bin/javaws.real
	install -m 755 javaws-wrapper.sh $J_INSTALL_DIR/jre/bin/javaws
fi

#install jar.binfmt
install -m 755 jar.binfmt $J_INSTALL_DIR/jre/lib/jar.binfmt

#clean up
rm -rf java-8-oracle

#To add when an older version exists:
#safe_move jdk-7u2-linux-x64.tar.gz #must be modified for each release
#safe_move jdk-7u2-linux-i586.tar.gz #must be modified for each release
rmdir $OLDDIR 2> /dev/null || true
#remove previous versions, if they exist


#db_fset oracle-java8-installer/local seen false

#This step is optional, recommended, and affects code below.
ls $J_INSTALL_DIR/man/man1/*.1 >/dev/null 2>&1 && \
  gzip -9 $J_INSTALL_DIR/man/man1/*.1 >/dev/null 2>&1

#Increment highest version by 1.
#Also assumes all Java helper programs (javaws, jcontrol, etc) at same version as java.
#These helpers should be slaves, or in the same path as java; thus, a reasonable assumption.

LATEST=1
LATEST=$((`LANG=C update-alternatives --display java | grep ^/ | sed -e 's/.* //g' | sort -n | tail -1`+1))

#create .java-8-oracle.jinfo file header:
if [ -e /usr/lib/jvm/.java-8-oracle.jinfo ]; then
   rm -f /usr/lib/jvm/.java-8-oracle.jinfo
fi
echo "name=java-8-oracle
alias=java-8-oracle
priority=$LATEST
section=non-free
" > /usr/lib/jvm/.java-8-oracle.jinfo


#link JRE files
for f in $J_INSTALL_DIR/jre/bin/*; do
    name=`basename $f`;
    if [ ! -f "/usr/bin/$name" -o -L "/usr/bin/$name" ]; then  #some files, like jvisualvm might not be links
        if [ -f "$J_INSTALL_DIR/man/man1/$name.1.gz" ]; then
					if [ ! $arch = "arm" ]; then
            update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/jre/bin/$name $LATEST --slave /usr/share/man/man1/$name.1.gz $name.1.gz $J_INSTALL_DIR/man/man1/$name.1.gz
            echo "jre $name $J_INSTALL_DIR/jre/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
					else
						# There's no javaws, jvisualvm or jmc on arm
						[ ! $name = "javaws" ] && [ ! $name = "jvisualvm" ] && [ ! $name = "jmc" ] && update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/jre/bin/$name $LATEST --slave /usr/share/man/man1/$name.1.gz $name.1.gz $J_INSTALL_DIR/man/man1/$name.1.gz
						[ ! $name = "javaws" ] && [ ! $name = "jvisualvm" ] && [ ! $name = "jmc" ] && echo "jre $name $J_INSTALL_DIR/jre/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
					fi
         else #no man pages available
            # [ ! $name = "javaws.real" ] = skip javaws.real     
            [ ! $name = "javaws.real" ] && update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/jre/bin/$name $LATEST
            [ ! $name = "javaws.real" ] && echo "jre $name $J_INSTALL_DIR/jre/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
         fi
    fi
done


#link JRE not in jre/bin
[ -f $J_INSTALL_DIR/jre/lib/jexec ]    && update-alternatives --install /usr/bin/jexec    jexec    $J_INSTALL_DIR/jre/lib/jexec    $LATEST && echo "jre jexec $J_INSTALL_DIR/jre/lib/jexec" >> /usr/lib/jvm/.java-8-oracle.jinfo

#This will issue ignorable warnings for alternatives that are not part of a group
#Link JDK files with/without man pages
if [ -d "$J_INSTALL_DIR/man/man1" ];then
    for f in $J_INSTALL_DIR/man/man1/*; do
        name=`basename $f .1.gz`;
        #some files, like jvisualvm might not be links. Further assume this for corresponding man page
        if [ ! -f "/usr/bin/$name" -o -L "/usr/bin/$name" ]; then
            if [ ! -f "$J_INSTALL_DIR/man/man1/$name.1.gz" ]; then
                name=`basename $f .1`;          #handle any legacy uncompressed pages
            fi
          		if [ ! -e $J_INSTALL_DIR/jre/bin/$name ]; then #don't link already linked JRE files
								if [ ! $arch = "arm" ]; then
              		update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/bin/$name $LATEST --slave /usr/share/man/man1/$name.1.gz $name.1.gz $J_INSTALL_DIR/man/man1/$name.1.gz
              		echo "jdk $name $J_INSTALL_DIR/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
								else
									# There's no javaws, jvisualvm or jmc on arm
									[ ! $name = "javaws" ] && [ ! $name = "jvisualvm" ] && [ ! $name = "jmc" ] && update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/bin/$name $LATEST --slave /usr/share/man/man1/$name.1.gz $name.1.gz $J_INSTALL_DIR/man/man1/$name.1.gz
									[ ! $name = "javaws" ] && [ ! $name = "jvisualvm" ] && [ ! $name = "jmc" ] && echo "jdk $name $J_INSTALL_DIR/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
								fi
          		fi
        fi
    done

else  #no man pages available
    for f in $J_INSTALL_DIR/bin/*; do
        name=`basename $f`;
        if [ ! -f "/usr/bin/$name" -o -L "/usr/bin/$name" ]; then  #some files, like jvisualvm might not be links
            if [ ! -e $J_INSTALL_DIR/jre/bin/$name ]; then #don't link already linked JRE files
                update-alternatives --install /usr/bin/$name $name $J_INSTALL_DIR/bin/$name $LATEST
                echo "jdk $name $J_INSTALL_DIR/bin/$name" >> /usr/lib/jvm/.java-8-oracle.jinfo
            fi
        fi
    done
fi

# Hide javaws and jvisualvm desktop files on arm since these files don't exist on this architecture
if [ $arch = "arm" ]; then
echo "NoDisplay=true" >> /usr/share/applications/JB-javaws-jdk8.desktop
echo "NoDisplay=true" >> /usr/share/applications/JB-jvisualvm-jdk8.desktop
fi

# register binfmt; ignore errors, the alternative may already be
# registered by another JRE.
#if which update-binfmts >/dev/null && [ -r /usr/share/binfmts/jar ]; then
    #update-binfmts --package oracle-java8 --import jar || true
#fi

echo "Oracle JDK 8 installed"

# Install Firefox (and compatible) plugin. $arch will be empty for unknown platform
# No plugin for arm architecture yet
[ -f $J_INSTALL_DIR/jre/lib/$arch/libnpjp2.so ] && \
	update-alternatives --install /usr/lib/mozilla/plugins/libjavaplugin.so mozilla-javaplugin.so $J_INSTALL_DIR/jre/lib/$arch/libnpjp2.so $LATEST && \
	echo "plugin mozilla-javaplugin.so $J_INSTALL_DIR/jre/lib/$arch/libnpjp2.so" >> /usr/lib/jvm/.java-8-oracle.jinfo && \
echo "Oracle JRE 8 browser plugin installed"

# Automatically added by dh_installmime
if [ "$1" = "configure" ] && [ -x "`which update-mime-database 2>/dev/null`" ]; then
	update-mime-database /usr/share/mime
fi
# End automatically added section
rm -rf "/var/cache/oracle-jdk8-installer"

exit 0

# vim: ts=2 sw=2
