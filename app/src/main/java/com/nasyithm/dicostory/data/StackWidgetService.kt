package com.nasyithm.dicostory.data

import android.content.Intent
import android.widget.RemoteViewsService
import com.nasyithm.dicostory.widget.StackRemoteViewsFactory

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        StackRemoteViewsFactory(this.applicationContext)
}