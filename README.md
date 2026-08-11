# Samsung Now Bar Proxy

An experimental Android notification proxy that allows individual app developers to forward an already-constructed `Notification` into Samsung's One UI ongoing notification / Now Bar system.

The proxy does **not** create, manage, or understand your application's events. Your application is responsible for creating the notification. The only interface exposed by this project is the broadcast receiver that accepts the notification and posts or removes it.

## Disclaimer

This project is **not affiliated with, endorsed by, sponsored by, or associated with KakaoTaxi, Kakao, Samsung, Google, or any other company whose names or package identifiers appear in this repository.**

The author has no relationship with KakaoTaxi and does not claim to represent it.

The `com.kakao.taxi` package name and associated broadcast actions are used solely because Samsung's notification handling gives this package identity the required treatment for the intended Now Bar behavior.

This project does not provide KakaoTaxi functionality, access Kakao services, use Google Maps, access user location, or communicate with any of these companies' services.

**Use of this technique is at your own risk. You are responsible for complying with applicable laws, platform policies, terms of service, and intellectual-property requirements.**

---

# What this project actually does

The proxy has one job:

> **Receive an already-built Android ****`Notification`**** through a broadcast and post it through the proxy application's package identity.**

Your application does everything else.

```text
Your application
       │
       │ Build Notification
       │
       ▼
com.kakao.taxi.action.POST
       │
       │ Notification + ID
       ▼
   ProxyReceiver
       │
       ▼
Android NotificationManager
       │
       ▼
Samsung One UI
   ├── Now Bar
   ├── ongoing notification
   └── other supported notification surfaces
```

---

# Proxy API

The proxy exposes two broadcast actions.

| Action                         | Purpose                         |
| ------------------------------ | ------------------------------- |
| `com.kakao.taxi.action.POST`   | Post/update a notification      |
| `com.kakao.taxi.action.CANCEL` | Remove an existing notification |

Both actions are sent explicitly to:

```text
com.kakao.taxi.ProxyReceiver
```

---

## POST

To post or update a notification, send:

```text
Action:
com.kakao.taxi.action.POST
```

to:

```text
com.kakao.taxi.ProxyReceiver
```

with these extras:

```text
com.kakao.taxi.extra.NOTIFICATION
com.kakao.taxi.extra.NOTIFICATION_ID
```

### `com.kakao.taxi.extra.NOTIFICATION`

Type:

```text
Notification
```

This must contain the notification that you want the proxy to post.

The proxy does not construct the notification contents.

For example, your own application can build:

```kotlin
val notification = NotificationCompat.Builder(...)
    // Your notification configuration
    .build()
```

and pass that notification to the proxy.

### `com.kakao.taxi.extra.NOTIFICATION_ID`

Type:

```text
Int
```

The notification ID used when posting the notification.

Use the same ID when cancelling it.

---

## Kotlin example

From the application that wants to use the proxy:

```kotlin
val intent = Intent("com.kakao.taxi.action.POST").apply {
    setClassName(
        "com.kakao.taxi",
        "com.kakao.taxi.ProxyReceiver"
    )

    putExtra(
        "com.kakao.taxi.extra.NOTIFICATION",
        notification
    )

    putExtra(
        "com.kakao.taxi.extra.NOTIFICATION_ID",
        NOTIFICATION_ID
    )
}

context.sendBroadcast(intent)
```

That is the entire integration point.

Your application can construct `notification` however it wants.

---

# CANCEL

To remove the notification, send:

```text
com.kakao.taxi.action.CANCEL
```

to the same receiver.

The only required extra is the notification ID:

```text
com.kakao.taxi.extra.NOTIFICATION_ID
```

Example:

```kotlin
val intent = Intent("com.kakao.taxi.action.CANCEL").apply {
    setClassName(
        "com.kakao.taxi",
        "com.kakao.taxi.ProxyReceiver"
    )

    putExtra(
        "com.kakao.taxi.extra.NOTIFICATION_ID",
        NOTIFICATION_ID
    )
}

context.sendBroadcast(intent)
```

The ID should match the notification that was previously posted.

---

# Notification configuration

The proxy does not require a specific application-level event structure.

However, if the goal is to make the resulting notification appear as a Samsung Now Bar / ongoing activity, the **notification itself** needs to contain the appropriate configuration.

The following values are used by the notification produced by the reference implementation.

These are documented here only as a reference for developers constructing their own notification.

## Ongoing notification

The reference notification uses:

```kotlin
.setOngoing(true)
.setOnlyAlertOnce(true)
.setRequestPromotedOngoing(true)
.setCategory(NotificationCompat.CATEGORY_EVENT)
.setPriority(NotificationCompat.PRIORITY_LOW)
.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
```

These settings are part of the notification supplied to the proxy. They are **not created by the proxy itself**.

---

# Samsung-specific notification extras

Samsung's ongoing activity / Now Bar implementation reads additional notification extras.

The reference notification uses the following.

### Ongoing activity style

```kotlin
putInt(
    "android.ongoingActivityNoti.style",
    1
)
```

### Secondary icon

```kotlin
putParcelable(
    "android.ongoingActivityNoti.secondIcon",
    Icon.createWithBitmap(bitmap)
)
```

### Now Bar chip

```kotlin
putInt(
    "android.ongoingActivityNoti.chipBgColor",
    color
)

putString(
    "android.ongoingActivityNoti.chipExpandedText",
    title
)
```

### Primary Now Bar information

```kotlin
putString(
    "android.ongoingActivityNoti.nowbarPrimaryInfo",
    "Now: $title"
)

putString(
    "android.ongoingActivityNoti.nowbarSecondaryInfo",
    durationText
)
```

### Expanded notification information

```kotlin
putString(
    "android.ongoingActivityNoti.primaryInfo",
    title
)

putString(
    "android.ongoingActivityNoti.secondaryInfo",
    subTitle
)
```

The meaning of these values is roughly:

| Extra                 | Purpose                                       |
| --------------------- | --------------------------------------------- |
| `style`               | Ongoing activity notification style           |
| `secondIcon`          | Icon displayed at the end of the Now Bar line |
| `chipBgColor`         | Chip background color                         |
| `chipExpandedText`    | Text displayed by the expanded chip           |
| `nowbarPrimaryInfo`   | Primary Now Bar text                          |
| `nowbarSecondaryInfo` | Secondary Now Bar text                        |
| `primaryInfo`         | Primary expanded notification text            |
| `secondaryInfo`       | Secondary expanded notification text          |

These extras are Samsung-specific implementation details rather than normal portable Android notification APIs.

They may change or stop working in future One UI versions.

---

# Samsung manifest metadata

The proxy application declares:

```xml
<meta-data
    android:name="com.samsung.android.support.ongoing_activity"
    android:value="true" />
```

This indicates support for Samsung's ongoing activity notification mechanism.

The receiver is exposed with the two proxy actions:

```xml
<receiver
    android:name=".ProxyReceiver"
    android:exported="true">

    <intent-filter>
        <action android:name="com.kakao.taxi.action.POST" />
        <action android:name="com.kakao.taxi.action.CANCEL" />
    </intent-filter>

</receiver>
```

---

# Notification permission

The proxy declares:

```xml
<uses-permission
    android:name="android.permission.POST_NOTIFICATIONS" />
```

The proxy checks this permission before posting a notification.

Therefore, the proxy application's notification permission must be granted on Android versions where `POST_NOTIFICATIONS` is a runtime permission.

The application sending the broadcast does not bypass Android's notification permission system.

---

# Why `com.kakao.taxi`?

The package identity is intentional.

The proxy uses:

```text
com.kakao.taxi
```

because Samsung's Now Bar / ongoing activity handling provides the required behavior for this package identity.

This is **not an official KakaoTaxi integration**.

The proxy does not connect to KakaoTaxi and does not use KakaoTaxi's APIs.

The package name is simply part of the workaround that makes the Samsung notification behavior available to the proxy.

Changing the package name may prevent the technique from working as intended.

---

# Icon

The proxy application uses:

```xml
android:icon="@drawable/ic_proxy"
```

The project does not provide a dynamic icon configuration system.

If you are building your own instance of the proxy, replace:

```text
res/drawable/ic_proxy
```

with your own application icon.

You should provide your own `ic_proxy` resource.

The icon used inside an individual notification is separate from the proxy application's launcher icon. Your application supplies those notification icons when constructing the `Notification`.

---

# Integration summary

Your application needs to do only this on the proxy side:

### Post

```text
Broadcast:
    com.kakao.taxi.action.POST

Extras:
    com.kakao.taxi.extra.NOTIFICATION
        → Notification

    com.kakao.taxi.extra.NOTIFICATION_ID
        → Int
```

### Remove

```text
Broadcast:
    com.kakao.taxi.action.CANCEL

Extras:
    com.kakao.taxi.extra.NOTIFICATION_ID
        → Int
```

Everything before these broadcasts is your application's responsibility.

```text
                    YOUR APPLICATION
                    ────────────────
                    Build Notification
                    Configure content
                    Configure icons
                    Configure Samsung extras
                    Decide when to post
                    Decide when to update
                    Decide when to stop
                           │
                           │
                           ▼
              ┌─────────────────────────┐
              │      PROXY INTERFACE    │
              │                         │
              │ POST  + Notification ID │
              │ CANCEL + Notification ID│
              └────────────┬────────────┘
                           │
                           ▼
                    ProxyReceiver
                           │
                           ▼
                 Android Notification
                       Manager
                           │
                           ▼
                    Samsung One UI
```

The proxy is intentionally this small. Your application owns the notification; this project merely provides the path through which it gets posted.

Huge thanks to [NowbarMeter](https://github.com/StarsShine11904/NowbarMeter) for showing that this is possible and implementation details