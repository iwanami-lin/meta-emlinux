LIC_FILE_CHKSUM_4.19 = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
LIC_FILE_CHKSUM_5.10 = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"
LIC_FILES_CHKSUM = "${@bb.utils.contains('LINUX_GIT_BRANCH', 'linux-5.10.y-cip', '${LIC_FILE_CHKSUM_5.10}', '${LIC_FILE_CHKSUM_4.19}', d)}"

DEPENDS += " rsync-native"
