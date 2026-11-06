package io.github.erenche.syncclipboard.xposed.sync

/**
 * 剪贴板 URI 的文件名解析（纯函数，便于单元测试）。
 *
 * 优先使用 provider 提供的真实文件名（OpenableColumns.DISPLAY_NAME），
 * 取不到时才退回 URI 最后一段。两者都会去掉目录部分和 Windows 非法字符，
 * 并在缺少扩展名时按 MIME 类型补上。
 *
 * 背景：MIUI 相册的 URI 最后一段解码后是完整路径（含 `/`），
 * MediaStore 的 URI 最后一段是数字 id（无扩展名），直接当文件名都会出错。
 */
internal object ClipFileName {

    private val ILLEGAL_CHARS = Regex("""[\u0000-\u001f<>:"|?*]""")
    private val EXTENSION = Regex("[a-z0-9]{1,10}")

    /**
     * @param displayName provider 返回的 DISPLAY_NAME，可能为 null
     * @param lastPathSegment URI 最后一段（已解码），可能为 null
     * @param extensionForMime MIME 对应的扩展名（不含点），未知时为 null
     * @param fallback 两者都不可用时的文件名（不含扩展名）
     */
    fun resolve(
        displayName: String?,
        lastPathSegment: String?,
        extensionForMime: String?,
        fallback: String
    ): String {
        val base = sanitize(displayName) ?: sanitize(lastPathSegment) ?: fallback
        return withExtension(base, extensionForMime)
    }

    /** 去掉目录部分与非法字符；结果为空或只剩 `.`/`..` 时返回 null。 */
    fun sanitize(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val leaf = raw.substringAfterLast('/').substringAfterLast('\\')
        val cleaned = leaf.replace(ILLEGAL_CHARS, "_").trim().trimEnd('.', ' ')
        if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") return null
        return cleaned
    }

    /** 名称没有扩展名且 MIME 扩展名有效时补上。 */
    fun withExtension(name: String, extension: String?): String {
        val ext = extension?.trim()?.trimStart('.')?.lowercase()
        if (ext.isNullOrEmpty() || !EXTENSION.matches(ext)) return name
        val dot = name.lastIndexOf('.')
        val hasExtension = dot > 0 && dot < name.length - 1
        return if (hasExtension) name else "$name.$ext"
    }
}
