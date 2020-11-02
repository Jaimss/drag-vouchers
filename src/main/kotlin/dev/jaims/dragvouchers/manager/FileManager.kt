package dev.jaims.dragvouchers.manager

import dev.jaims.dragvouchers.DragVouchers

class FileManager(plugin: DragVouchers) {

    init {
        plugin.saveConfig()
    }

}