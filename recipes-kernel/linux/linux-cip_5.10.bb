require recipes-kernel/linux/linux-base_git.bb

PROVIDES += " linux-cip"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI_append = " \
	file://base.config \
"

FILESEXTRAPATHS_append := ":${THISDIR}/../../../meta-debian/recipes-kernel/linux/files/"

LINUX_GIT_BRANCH = "linux-5.10.y-cip"
LINUX_GIT_SRCREV = "3ddbe9bf6a006b50d35887b00f96c5768a32b7f3"
LINUX_CVE_VERSION = "${@bb.utils.contains('LINUX_GIT_SRCREV', '3ddbe9bf6a006b50d35887b00f96c5768a32b7f3', '5.10.8', '${PV}', d)}"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

KERNEL_CONFIG_COMMAND = "oe_runmake_call O=${B} -C ${S} olddefconfig"

SRC_URI_append_qemuall += "file://qemu-emlinux.config"
SRC_URI_append_qemuarm += "file://qemuarm.config"

SRC_URI_append_raspberrypi3-64 += "file://raspberrypi3-64.config"
LINUX_DEFCONFIG_raspberrypi3-64 = "defconfig"
KERNEL_IMAGETYPE_raspberrypi3-64 = "Image"
KERNEL_DEVICETREE_raspberrypi3-64 = "broadcom/bcm2837-rpi-3-b-plus.dtb \
                                     broadcom/bcm2837-rpi-3-b.dtb"

SRC_URI_append_beaglebone += "file://beaglebone.config"

CVE_VERSION = "${LINUX_CVE_VERSION}"

do_shared_workdir () {
        cd ${B}

        kerneldir=${STAGING_KERNEL_BUILDDIR}
        install -d $kerneldir

        #
        # Store the kernel version in sysroots for module-base.bbclass
        #

        echo "${KERNEL_VERSION}" > $kerneldir/${KERNEL_PACKAGE_NAME}-abiversion

        # Copy files required for module builds
        cp System.map $kerneldir/System.map-${KERNEL_VERSION}
        [ -e Module.symvers ] && cp Module.symvers $kerneldir/
        cp .config $kerneldir/
        mkdir -p $kerneldir/include/config
        cp include/config/kernel.release $kerneldir/include/config/kernel.release
        if [ -e certs/signing_key.x509 ]; then
                # The signing_key.* files are stored in the certs/ dir in
                # newer Linux kernels
                mkdir -p $kerneldir/certs
                cp certs/signing_key.* $kerneldir/certs/
        elif [ -e signing_key.priv ]; then
                cp signing_key.* $kerneldir/
        fi

        # We can also copy over all the generated files and avoid special cases
        # like version.h, but we've opted to keep this small until file creep starts
        # to happen
        if [ -e include/linux/version.h ]; then
                mkdir -p $kerneldir/include/linux
                cp include/linux/version.h $kerneldir/include/linux/version.h
        fi

        # As of Linux kernel version 3.0.1, the clean target removes
        # arch/powerpc/lib/crtsavres.o which is present in
        # KBUILD_LDFLAGS_MODULE, making it required to build external modules.
        if [ ${ARCH} = "powerpc" ]; then
                if [ -e arch/powerpc/lib/crtsavres.o ]; then
                        mkdir -p $kerneldir/arch/powerpc/lib/
                        cp arch/powerpc/lib/crtsavres.o $kerneldir/arch/powerpc/lib/crtsavres.o
                fi
        fi

        if [ -d include/generated ]; then
                mkdir -p $kerneldir/include/generated/
                cp -fR include/generated/* $kerneldir/include/generated/
        fi

        if [ -d arch/${ARCH}/include/generated ]; then
                mkdir -p $kerneldir/arch/${ARCH}/include/generated/
                cp -fR arch/${ARCH}/include/generated/* $kerneldir/arch/${ARCH}/include/generated/
        fi

        if (grep -q -i -e '^CONFIG_UNWINDER_ORC=y$' $kerneldir/.config); then
                # With CONFIG_UNWINDER_ORC (the default in 4.14), objtool is required for
                # out-of-tree modules to be able to generate object files.
                if [ -x tools/objtool/objtool ]; then
                        mkdir -p ${kerneldir}/tools/objtool
                        cp tools/objtool/objtool ${kerneldir}/tools/objtool/
                fi
        fi
}
