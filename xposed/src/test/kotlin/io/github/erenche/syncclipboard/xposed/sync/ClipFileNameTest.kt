package io.github.erenche.syncclipboard.xposed.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipFileNameTest {

    @Test fun displayNameWins() {
        assertEquals(
            "Screenshot_2026-11-06-08-49-49-432_com.netease.uuremote.jpg",
            ClipFileName.resolve(
                "Screenshot_2026-11-06-08-49-49-432_com.netease.uuremote.jpg", "1452", "jpg", "file_1"
            )
        )
    }

    @Test fun mediaStoreIdGetsExtensionWhenNoDisplayName() {
        assertEquals("1452.jpg", ClipFileName.resolve(null, "1452", "jpg", "file_1"))
    }

    @Test fun miuiGalleryPathIsReducedToLeaf() {
        // content://com.miui.gallery.open/raw/%2Fstorage%2F... 解码后的 lastPathSegment
        assertEquals(
            "Screenshot_x.jpg",
            ClipFileName.resolve(null, "/storage/emulated/0/DCIM/Screenshots/Screenshot_x.jpg", "jpeg", "file_1")
        )
    }

    @Test fun existingExtensionIsKept() {
        assertEquals("photo.png", ClipFileName.resolve("photo.png", null, "jpg", "file_1"))
    }

    @Test fun fallbackUsedWhenNothingUsable() {
        assertEquals("file_1.png", ClipFileName.resolve(" ", "..", "png", "file_1"))
        assertEquals("file_1", ClipFileName.resolve(null, null, null, "file_1"))
    }

    @Test fun windowsIllegalCharactersReplaced() {
        assertEquals("a_b_c.txt", ClipFileName.resolve("a:b?c.txt", null, null, "file_1"))
        assertEquals("名字 1.jpg", ClipFileName.resolve("名字 1", null, "jpg", "file_1"))
    }

    @Test fun sanitizeRejectsEmptyAndDots() {
        assertNull(ClipFileName.sanitize(""))
        assertNull(ClipFileName.sanitize("dir/"))
        assertNull(ClipFileName.sanitize("."))
        assertNull(ClipFileName.sanitize(".."))
    }

    @Test fun invalidMimeExtensionIgnored() {
        assertEquals("report", ClipFileName.withExtension("report", "x/y"))
        assertEquals("report", ClipFileName.withExtension("report", ""))
        assertEquals("archive.tar.gz", ClipFileName.withExtension("archive.tar.gz", "gz"))
    }
}
