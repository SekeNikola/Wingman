package com.wingman.launcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.XmlResourceParser
import android.util.Xml
import androidx.compose.ui.graphics.ImageBitmap
import com.wingman.launcher.data.model.AppShortcut
import org.xmlpull.v1.XmlPullParser

object IconPackLoader {

    private val ICON_PACK_ACTIONS = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "com.gau.go.launcherex.theme"
    )

    fun findIconPacks(pm: PackageManager): List<AppShortcut> =
        ICON_PACK_ACTIONS
            .flatMap { action -> pm.queryIntentActivities(Intent(action), 0) }
            .distinctBy { it.activityInfo.packageName }
            .mapNotNull { ri ->
                runCatching {
                    AppShortcut(
                        packageName = ri.activityInfo.packageName,
                        label       = ri.loadLabel(pm).toString(),
                        icon        = ri.loadIcon(pm).toImageBitmap()
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase() }

    fun loadIconForApp(
        context: Context,
        iconPackPackage: String,
        targetPackage: String
    ): ImageBitmap? = runCatching {
        val drawableName = findDrawableName(context, iconPackPackage, targetPackage) ?: return null
        val res = context.packageManager.getResourcesForApplication(iconPackPackage)
        val id  = res.getIdentifier(drawableName, "drawable", iconPackPackage)
        if (id == 0) return null
        @Suppress("DEPRECATION")
        res.getDrawable(id)?.toImageBitmap()
    }.getOrNull()

    private fun findDrawableName(
        context: Context,
        iconPackPackage: String,
        targetPackage: String
    ): String? {
        // Most icon packs (including Pixbit) store appfilter in res/xml/
        runCatching {
            val res   = context.packageManager.getResourcesForApplication(iconPackPackage)
            val xmlId = res.getIdentifier("appfilter", "xml", iconPackPackage)
            if (xmlId != 0) {
                val result = parseXmlParser(res.getXml(xmlId), targetPackage)
                if (result != null) return result
            }
        }

        // Fallback: some older packs store it in assets/
        runCatching {
            val packCtx = context.createPackageContext(iconPackPackage, 0)
            val stream  = packCtx.assets.open("appfilter.xml")
            val parser  = Xml.newPullParser().apply { setInput(stream, null) }
            val result  = parsePullParser(parser, targetPackage)
            stream.close()
            if (result != null) return result
        }

        return null
    }

    // Handles XmlResourceParser (from res/xml/) — it IS an XmlPullParser
    private fun parseXmlParser(parser: XmlResourceParser, targetPackage: String): String? =
        runCatching {
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG &&
                    (parser.name == "item" || parser.name == "AppMap")
                ) {
                    val component = parser.getAttributeValue(null, "component") ?: ""
                    val drawable  = parser.getAttributeValue(null, "drawable")
                    if (drawable != null && extractPackage(component) == targetPackage) {
                        return drawable
                    }
                }
                parser.next()
            }
            null
        }.getOrNull()

    private fun parsePullParser(parser: XmlPullParser, targetPackage: String): String? =
        runCatching {
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG &&
                    (parser.name == "item" || parser.name == "AppMap")
                ) {
                    val component = parser.getAttributeValue(null, "component") ?: ""
                    val drawable  = parser.getAttributeValue(null, "drawable")
                    if (drawable != null && extractPackage(component) == targetPackage) {
                        return drawable
                    }
                }
                event = parser.next()
            }
            null
        }.getOrNull()

    // "ComponentInfo{com.pkg/com.pkg.Activity}" → "com.pkg"
    // Also handles bare "com.pkg/activity" format
    private fun extractPackage(component: String): String {
        val inner = component
            .removePrefix("ComponentInfo{")
            .removeSuffix("}")
        return inner.substringBefore("/").trim()
    }
}
