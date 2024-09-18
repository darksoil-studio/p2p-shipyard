package com.plugin.holochainforegroundservice

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.content.ServiceConnection
import android.content.ComponentName
import android.os.IBinder
import app.tauri.annotation.Command
import app.tauri.annotation.InvokeArg
import app.tauri.annotation.TauriPlugin
import app.tauri.plugin.JSObject
import app.tauri.plugin.JSArray
import app.tauri.plugin.Plugin
import app.tauri.plugin.Invoke
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import com.plugin.holochainforegroundservice.toJSArray

@TauriPlugin
class HolochainPlugin(private val activity: Activity): Plugin(activity) {
    private var mService: IHolochainService? = null
    private lateinit var webView: WebView

    private val LOG_TAG = "HolochainPlugin"
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = IHolochainService.Stub.asInterface(service)
            Log.d(LOG_TAG, "IHolochainService connected")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            Log.d(LOG_TAG, "IHolochainService disconnected")
        }
    }

    /// Load the plugin, start the service
    override fun load(webView: WebView) {
        super.load(webView)
        this.webView = webView

        // Start the service
        runBlocking {
            launchInternal()
        }
    }

    /// Start the service
    /// - Starts the foreground service
    /// - Launches a conductor
    /// - Creates an admin websocket
    @Command
    fun launch(invoke: Invoke) {
        val args = invoke.parseArgs(HolochainArgs::class.java)
        launchInternal()
        invoke.resolve()
    }
    
    /// Stop the service
    @Command
    fun shutdown(invoke: Invoke) {
        this.mService?.shutdown()
        invoke.resolve()
    }

    /// Get the holochain conductor admin websocket port
    @Command
    fun getAdminPort(invoke: Invoke) {
        val res: Int? = this.mService?.getAdminPort()
        val obj = JSObject()
        obj.put("port", res)
        invoke.resolve(obj)
    }

    /// Install a happ into conductor
    @Command
    fun installApp(invoke: Invoke) {
        val args = invoke.parseArgs(InstallAppRequestArgs::class.java)
        this.mService?.installApp(InstallAppRequestAidl(
            args.appId,
            args.appBundleBytes,
            args.membraneProofs,
            args.agent,
            args.networkSeed
        ))
        invoke.resolve()
    }

    /// List installed happs in conductor
    @Command
    fun listInstalledApps(invoke: Invoke) {
        val res = this.mService?.listInstalledApps()
        val obj = JSObject();
        obj.put("installedApps", res!!.toJSArray())
        invoke.resolve(obj)
    }

    /// Get or create an app websocket with authentication token
    @Command
    fun appWebsocketAuth(invoke: Invoke) {
        val args = invoke.parseArgs(AppWebsocketAuthRequestArgs::class.java)
        val res = this.mService?.appWebsocketAuth(args.appId)

        // Inject launcher env into web view
        this.injectHolochainClientEnv(res!!.port, args.appId, res!!.token)      
        
        val obj = JSObject();
        obj.put("appWebsocketAuth", res!!.toJSObject())
        invoke.resolve(obj)       
    }

    private fun injectHolochainClientEnv(appWebsocketPort: Int, appId: String, appWebsocketToken: UByteArray) {
        val token = appWebsocketToken.toMutableList().toJSArray();
        this.webView.evaluateJavascript("""
            // Minified @msgpack/msgpack library v2.8.0
            !function(e,t){"object"==typeof exports&&"object"==typeof module?module.exports=t():"function"==typeof define&&define.amd?define([],t):"object"==typeof exports?exports.MessagePack=t():e.MessagePack=t()}(this,(function(){return function(){"use strict";var e={d:function(t,n){for(var r in n)e.o(n,r)&&!e.o(t,r)&&Object.defineProperty(t,r,{enumerable:!0,get:n[r]})},o:function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},r:function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})}},t={};e.r(t),e.d(t,{DataViewIndexOutOfBoundsError:function(){return K},DecodeError:function(){return m},Decoder:function(){return G},EXT_TIMESTAMP:function(){return U},Encoder:function(){return z},ExtData:function(){return b},ExtensionCodec:function(){return A},decode:function(){return q},decodeArrayStream:function(){return ne},decodeAsync:function(){return te},decodeMulti:function(){return J},decodeMultiStream:function(){return re},decodeStream:function(){return ie},decodeTimestampExtension:function(){return L},decodeTimestampToTimeSpec:function(){return T},encode:function(){return C},encodeDateToTimeSpec:function(){return E},encodeTimeSpecToTimestamp:function(){return S},encodeTimestampExtension:function(){return B}});var n=4294967295;function r(e,t,n){var r=Math.floor(n/4294967296),i=n;e.setUint32(t,r),e.setUint32(t+4,i)}function i(e,t){return 4294967296*e.getInt32(t)+e.getUint32(t+4)}var o,s,a,c=function(e,t){var n="function"==typeof Symbol&&e[Symbol.iterator];if(!n)return e;var r,i,o=n.call(e),s=[];try{for(;(void 0===t||t-- >0)&&!(r=o.next()).done;)s.push(r.value)}catch(e){i={error:e}}finally{try{r&&!r.done&&(n=o.return)&&n.call(o)}finally{if(i)throw i.error}}return s},h=function(e,t,n){if(n||2===arguments.length)for(var r,i=0,o=t.length;i<o;i++)!r&&i in t||(r||(r=Array.prototype.slice.call(t,0,i)),r[i]=t[i]);return e.concat(r||Array.prototype.slice.call(t))},u=("undefined"==typeof process||"never"!==(null===(o=null===process||void 0===process?void 0:process.env)||void 0===o?void 0:o.TEXT_ENCODING))&&"undefined"!=typeof TextEncoder&&"undefined"!=typeof TextDecoder;function f(e){for(var t=e.length,n=0,r=0;r<t;){var i=e.charCodeAt(r++);if(0!=(4294967168&i))if(0==(4294965248&i))n+=2;else{if(i>=55296&&i<=56319&&r<t){var o=e.charCodeAt(r);56320==(64512&o)&&(++r,i=((1023&i)<<10)+(1023&o)+65536)}n+=0==(4294901760&i)?3:4}else n++}return n}var l=u?new TextEncoder:void 0,p=u?"undefined"!=typeof process&&"force"!==(null===(s=null===process||void 0===process?void 0:process.env)||void 0===s?void 0:s.TEXT_ENCODING)?200:0:n,d=(null==l?void 0:l.encodeInto)?function(e,t,n){l.encodeInto(e,t.subarray(n))}:function(e,t,n){t.set(l.encode(e),n)};function y(e,t,n){for(var r=t,i=r+n,o=[],s="";r<i;){var a=e[r++];if(0==(128&a))o.push(a);else if(192==(224&a)){var u=63&e[r++];o.push((31&a)<<6|u)}else if(224==(240&a)){u=63&e[r++];var f=63&e[r++];o.push((31&a)<<12|u<<6|f)}else if(240==(248&a)){var l=(7&a)<<18|(u=63&e[r++])<<12|(f=63&e[r++])<<6|63&e[r++];l>65535&&(l-=65536,o.push(l>>>10&1023|55296),l=56320|1023&l),o.push(l)}else o.push(a);o.length>=4096&&(s+=String.fromCharCode.apply(String,h([],c(o),!1)),o.length=0)}return o.length>0&&(s+=String.fromCharCode.apply(String,h([],c(o),!1))),s}var w,v=u?new TextDecoder:null,g=u?"undefined"!=typeof process&&"force"!==(null===(a=null===process||void 0===process?void 0:process.env)||void 0===a?void 0:a.TEXT_DECODER)?200:0:n,b=function(e,t){this.type=e,this.data=t},x=(w=function(e,t){return w=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(e,t){e.__proto__=t}||function(e,t){for(var n in t)Object.prototype.hasOwnProperty.call(t,n)&&(e[n]=t[n])},w(e,t)},function(e,t){if("function"!=typeof t&&null!==t)throw new TypeError("Class extends value "+String(t)+" is not a constructor or null");function n(){this.constructor=e}w(e,t),e.prototype=null===t?Object.create(t):(n.prototype=t.prototype,new n)}),m=function(e){function t(n){var r=e.call(this,n)||this,i=Object.create(t.prototype);return Object.setPrototypeOf(r,i),Object.defineProperty(r,"name",{configurable:!0,enumerable:!1,value:t.name}),r}return x(t,e),t}(Error),U=-1;function S(e){var t,n=e.sec,i=e.nsec;if(n>=0&&i>=0&&n<=17179869183){if(0===i&&n<=4294967295){var o=new Uint8Array(4);return(t=new DataView(o.buffer)).setUint32(0,n),o}var s=n/4294967296,a=4294967295&n;return o=new Uint8Array(8),(t=new DataView(o.buffer)).setUint32(0,i<<2|3&s),t.setUint32(4,a),o}return o=new Uint8Array(12),(t=new DataView(o.buffer)).setUint32(0,i),r(t,4,n),o}function E(e){var t=e.getTime(),n=Math.floor(t/1e3),r=1e6*(t-1e3*n),i=Math.floor(r/1e9);return{sec:n+i,nsec:r-1e9*i}}function B(e){return e instanceof Date?S(E(e)):null}function T(e){var t=new DataView(e.buffer,e.byteOffset,e.byteLength);switch(e.byteLength){case 4:return{sec:t.getUint32(0),nsec:0};case 8:var n=t.getUint32(0);return{sec:4294967296*(3&n)+t.getUint32(4),nsec:n>>>2};case 12:return{sec:i(t,4),nsec:t.getUint32(0)};default:throw new m("Unrecognized data size for timestamp (expected 4, 8, or 12): ".concat(e.length))}}function L(e){var t=T(e);return new Date(1e3*t.sec+t.nsec/1e6)}var I={type:U,encode:B,decode:L},A=function(){function e(){this.builtInEncoders=[],this.builtInDecoders=[],this.encoders=[],this.decoders=[],this.register(I)}return e.prototype.register=function(e){var t=e.type,n=e.encode,r=e.decode;if(t>=0)this.encoders[t]=n,this.decoders[t]=r;else{var i=1+t;this.builtInEncoders[i]=n,this.builtInDecoders[i]=r}},e.prototype.tryToEncode=function(e,t){for(var n=0;n<this.builtInEncoders.length;n++)if(null!=(r=this.builtInEncoders[n])&&null!=(i=r(e,t)))return new b(-1-n,i);for(n=0;n<this.encoders.length;n++){var r,i;if(null!=(r=this.encoders[n])&&null!=(i=r(e,t)))return new b(n,i)}return e instanceof b?e:null},e.prototype.decode=function(e,t,n){var r=t<0?this.builtInDecoders[-1-t]:this.decoders[t];return r?r(e,t,n):new b(t,e)},e.defaultCodec=new e,e}();function k(e){return e instanceof Uint8Array?e:ArrayBuffer.isView(e)?new Uint8Array(e.buffer,e.byteOffset,e.byteLength):e instanceof ArrayBuffer?new Uint8Array(e):Uint8Array.from(e)}var M=function(e){var t="function"==typeof Symbol&&Symbol.iterator,n=t&&e[t],r=0;if(n)return n.call(e);if(e&&"number"==typeof e.length)return{next:function(){return e&&r>=e.length&&(e=void 0),{value:e&&e[r++],done:!e}}};throw new TypeError(t?"Object is not iterable.":"Symbol.iterator is not defined.")},z=function(){function e(e,t,n,r,i,o,s,a){void 0===e&&(e=A.defaultCodec),void 0===t&&(t=void 0),void 0===n&&(n=100),void 0===r&&(r=2048),void 0===i&&(i=!1),void 0===o&&(o=!1),void 0===s&&(s=!1),void 0===a&&(a=!1),this.extensionCodec=e,this.context=t,this.maxDepth=n,this.initialBufferSize=r,this.sortKeys=i,this.forceFloat32=o,this.ignoreUndefined=s,this.forceIntegerToFloat=a,this.pos=0,this.view=new DataView(new ArrayBuffer(this.initialBufferSize)),this.bytes=new Uint8Array(this.view.buffer)}return e.prototype.reinitializeState=function(){this.pos=0},e.prototype.encodeSharedRef=function(e){return this.reinitializeState(),this.doEncode(e,1),this.bytes.subarray(0,this.pos)},e.prototype.encode=function(e){return this.reinitializeState(),this.doEncode(e,1),this.bytes.slice(0,this.pos)},e.prototype.doEncode=function(e,t){if(t>this.maxDepth)throw new Error("Too deep objects in depth ".concat(t));null==e?this.encodeNil():"boolean"==typeof e?this.encodeBoolean(e):"number"==typeof e?this.encodeNumber(e):"string"==typeof e?this.encodeString(e):this.encodeObject(e,t)},e.prototype.ensureBufferSizeToWrite=function(e){var t=this.pos+e;this.view.byteLength<t&&this.resizeBuffer(2*t)},e.prototype.resizeBuffer=function(e){var t=new ArrayBuffer(e),n=new Uint8Array(t),r=new DataView(t);n.set(this.bytes),this.view=r,this.bytes=n},e.prototype.encodeNil=function(){this.writeU8(192)},e.prototype.encodeBoolean=function(e){!1===e?this.writeU8(194):this.writeU8(195)},e.prototype.encodeNumber=function(e){Number.isSafeInteger(e)&&!this.forceIntegerToFloat?e>=0?e<128?this.writeU8(e):e<256?(this.writeU8(204),this.writeU8(e)):e<65536?(this.writeU8(205),this.writeU16(e)):e<4294967296?(this.writeU8(206),this.writeU32(e)):(this.writeU8(207),this.writeU64(e)):e>=-32?this.writeU8(224|e+32):e>=-128?(this.writeU8(208),this.writeI8(e)):e>=-32768?(this.writeU8(209),this.writeI16(e)):e>=-2147483648?(this.writeU8(210),this.writeI32(e)):(this.writeU8(211),this.writeI64(e)):this.forceFloat32?(this.writeU8(202),this.writeF32(e)):(this.writeU8(203),this.writeF64(e))},e.prototype.writeStringHeader=function(e){if(e<32)this.writeU8(160+e);else if(e<256)this.writeU8(217),this.writeU8(e);else if(e<65536)this.writeU8(218),this.writeU16(e);else{if(!(e<4294967296))throw new Error("Too long string: ".concat(e," bytes in UTF-8"));this.writeU8(219),this.writeU32(e)}},e.prototype.encodeString=function(e){if(e.length>p){var t=f(e);this.ensureBufferSizeToWrite(5+t),this.writeStringHeader(t),d(e,this.bytes,this.pos),this.pos+=t}else t=f(e),this.ensureBufferSizeToWrite(5+t),this.writeStringHeader(t),function(e,t,n){for(var r=e.length,i=n,o=0;o<r;){var s=e.charCodeAt(o++);if(0!=(4294967168&s)){if(0==(4294965248&s))t[i++]=s>>6&31|192;else{if(s>=55296&&s<=56319&&o<r){var a=e.charCodeAt(o);56320==(64512&a)&&(++o,s=((1023&s)<<10)+(1023&a)+65536)}0==(4294901760&s)?(t[i++]=s>>12&15|224,t[i++]=s>>6&63|128):(t[i++]=s>>18&7|240,t[i++]=s>>12&63|128,t[i++]=s>>6&63|128)}t[i++]=63&s|128}else t[i++]=s}}(e,this.bytes,this.pos),this.pos+=t},e.prototype.encodeObject=function(e,t){var n=this.extensionCodec.tryToEncode(e,this.context);if(null!=n)this.encodeExtension(n);else if(Array.isArray(e))this.encodeArray(e,t);else if(ArrayBuffer.isView(e))this.encodeBinary(e);else{if("object"!=typeof e)throw new Error("Unrecognized object: ".concat(Object.prototype.toString.apply(e)));this.encodeMap(e,t)}},e.prototype.encodeBinary=function(e){var t=e.byteLength;if(t<256)this.writeU8(196),this.writeU8(t);else if(t<65536)this.writeU8(197),this.writeU16(t);else{if(!(t<4294967296))throw new Error("Too large binary: ".concat(t));this.writeU8(198),this.writeU32(t)}var n=k(e);this.writeU8a(n)},e.prototype.encodeArray=function(e,t){var n,r,i=e.length;if(i<16)this.writeU8(144+i);else if(i<65536)this.writeU8(220),this.writeU16(i);else{if(!(i<4294967296))throw new Error("Too large array: ".concat(i));this.writeU8(221),this.writeU32(i)}try{for(var o=M(e),s=o.next();!s.done;s=o.next()){var a=s.value;this.doEncode(a,t+1)}}catch(e){n={error:e}}finally{try{s&&!s.done&&(r=o.return)&&r.call(o)}finally{if(n)throw n.error}}},e.prototype.countWithoutUndefined=function(e,t){var n,r,i=0;try{for(var o=M(t),s=o.next();!s.done;s=o.next())void 0!==e[s.value]&&i++}catch(e){n={error:e}}finally{try{s&&!s.done&&(r=o.return)&&r.call(o)}finally{if(n)throw n.error}}return i},e.prototype.encodeMap=function(e,t){var n,r,i=Object.keys(e);this.sortKeys&&i.sort();var o=this.ignoreUndefined?this.countWithoutUndefined(e,i):i.length;if(o<16)this.writeU8(128+o);else if(o<65536)this.writeU8(222),this.writeU16(o);else{if(!(o<4294967296))throw new Error("Too large map object: ".concat(o));this.writeU8(223),this.writeU32(o)}try{for(var s=M(i),a=s.next();!a.done;a=s.next()){var c=a.value,h=e[c];this.ignoreUndefined&&void 0===h||(this.encodeString(c),this.doEncode(h,t+1))}}catch(e){n={error:e}}finally{try{a&&!a.done&&(r=s.return)&&r.call(s)}finally{if(n)throw n.error}}},e.prototype.encodeExtension=function(e){var t=e.data.length;if(1===t)this.writeU8(212);else if(2===t)this.writeU8(213);else if(4===t)this.writeU8(214);else if(8===t)this.writeU8(215);else if(16===t)this.writeU8(216);else if(t<256)this.writeU8(199),this.writeU8(t);else if(t<65536)this.writeU8(200),this.writeU16(t);else{if(!(t<4294967296))throw new Error("Too large extension object: ".concat(t));this.writeU8(201),this.writeU32(t)}this.writeI8(e.type),this.writeU8a(e.data)},e.prototype.writeU8=function(e){this.ensureBufferSizeToWrite(1),this.view.setUint8(this.pos,e),this.pos++},e.prototype.writeU8a=function(e){var t=e.length;this.ensureBufferSizeToWrite(t),this.bytes.set(e,this.pos),this.pos+=t},e.prototype.writeI8=function(e){this.ensureBufferSizeToWrite(1),this.view.setInt8(this.pos,e),this.pos++},e.prototype.writeU16=function(e){this.ensureBufferSizeToWrite(2),this.view.setUint16(this.pos,e),this.pos+=2},e.prototype.writeI16=function(e){this.ensureBufferSizeToWrite(2),this.view.setInt16(this.pos,e),this.pos+=2},e.prototype.writeU32=function(e){this.ensureBufferSizeToWrite(4),this.view.setUint32(this.pos,e),this.pos+=4},e.prototype.writeI32=function(e){this.ensureBufferSizeToWrite(4),this.view.setInt32(this.pos,e),this.pos+=4},e.prototype.writeF32=function(e){this.ensureBufferSizeToWrite(4),this.view.setFloat32(this.pos,e),this.pos+=4},e.prototype.writeF64=function(e){this.ensureBufferSizeToWrite(8),this.view.setFloat64(this.pos,e),this.pos+=8},e.prototype.writeU64=function(e){this.ensureBufferSizeToWrite(8),function(e,t,n){var r=n/4294967296,i=n;e.setUint32(t,r),e.setUint32(t+4,i)}(this.view,this.pos,e),this.pos+=8},e.prototype.writeI64=function(e){this.ensureBufferSizeToWrite(8),r(this.view,this.pos,e),this.pos+=8},e}(),D={};function C(e,t){return void 0===t&&(t=D),new z(t.extensionCodec,t.context,t.maxDepth,t.initialBufferSize,t.sortKeys,t.forceFloat32,t.ignoreUndefined,t.forceIntegerToFloat).encodeSharedRef(e)}function P(e){return"".concat(e<0?"-":"","0x").concat(Math.abs(e).toString(16).padStart(2,"0"))}var O=function(){function e(e,t){void 0===e&&(e=16),void 0===t&&(t=16),this.maxKeyLength=e,this.maxLengthPerKey=t,this.hit=0,this.miss=0,this.caches=[];for(var n=0;n<this.maxKeyLength;n++)this.caches.push([])}return e.prototype.canBeCached=function(e){return e>0&&e<=this.maxKeyLength},e.prototype.find=function(e,t,n){var r,i,o=this.caches[n-1];try{e:for(var s=function(e){var t="function"==typeof Symbol&&Symbol.iterator,n=t&&e[t],r=0;if(n)return n.call(e);if(e&&"number"==typeof e.length)return{next:function(){return e&&r>=e.length&&(e=void 0),{value:e&&e[r++],done:!e}}};throw new TypeError(t?"Object is not iterable.":"Symbol.iterator is not defined.")}(o),a=s.next();!a.done;a=s.next()){for(var c=a.value,h=c.bytes,u=0;u<n;u++)if(h[u]!==e[t+u])continue e;return c.str}}catch(e){r={error:e}}finally{try{a&&!a.done&&(i=s.return)&&i.call(s)}finally{if(r)throw r.error}}return null},e.prototype.store=function(e,t){var n=this.caches[e.length-1],r={bytes:e,str:t};n.length>=this.maxLengthPerKey?n[Math.random()*n.length|0]=r:n.push(r)},e.prototype.decode=function(e,t,n){var r=this.find(e,t,n);if(null!=r)return this.hit++,r;this.miss++;var i=y(e,t,n),o=Uint8Array.prototype.slice.call(e,t,t+n);return this.store(o,i),i},e}(),_=function(e,t){var n,r,i,o,s={label:0,sent:function(){if(1&i[0])throw i[1];return i[1]},trys:[],ops:[]};return o={next:a(0),throw:a(1),return:a(2)},"function"==typeof Symbol&&(o[Symbol.iterator]=function(){return this}),o;function a(o){return function(a){return function(o){if(n)throw new TypeError("Generator is already executing.");for(;s;)try{if(n=1,r&&(i=2&o[0]?r.return:o[0]?r.throw||((i=r.return)&&i.call(r),0):r.next)&&!(i=i.call(r,o[1])).done)return i;switch(r=0,i&&(o=[2&o[0],i.value]),o[0]){case 0:case 1:i=o;break;case 4:return s.label++,{value:o[1],done:!1};case 5:s.label++,r=o[1],o=[0];continue;case 7:o=s.ops.pop(),s.trys.pop();continue;default:if(!((i=(i=s.trys).length>0&&i[i.length-1])||6!==o[0]&&2!==o[0])){s=0;continue}if(3===o[0]&&(!i||o[1]>i[0]&&o[1]<i[3])){s.label=o[1];break}if(6===o[0]&&s.label<i[1]){s.label=i[1],i=o;break}if(i&&s.label<i[2]){s.label=i[2],s.ops.push(o);break}i[2]&&s.ops.pop(),s.trys.pop();continue}o=t.call(e,s)}catch(e){o=[6,e],r=0}finally{n=i=0}if(5&o[0])throw o[1];return{value:o[0]?o[1]:void 0,done:!0}}([o,a])}}},j=function(e){if(!Symbol.asyncIterator)throw new TypeError("Symbol.asyncIterator is not defined.");var t,n=e[Symbol.asyncIterator];return n?n.call(e):(e="function"==typeof __values?__values(e):e[Symbol.iterator](),t={},r("next"),r("throw"),r("return"),t[Symbol.asyncIterator]=function(){return this},t);function r(n){t[n]=e[n]&&function(t){return new Promise((function(r,i){!function(e,t,n,r){Promise.resolve(r).then((function(t){e({value:t,done:n})}),t)}(r,i,(t=e[n](t)).done,t.value)}))}}},F=function(e){return this instanceof F?(this.v=e,this):new F(e)},W=function(e,t,n){if(!Symbol.asyncIterator)throw new TypeError("Symbol.asyncIterator is not defined.");var r,i=n.apply(e,t||[]),o=[];return r={},s("next"),s("throw"),s("return"),r[Symbol.asyncIterator]=function(){return this},r;function s(e){i[e]&&(r[e]=function(t){return new Promise((function(n,r){o.push([e,t,n,r])>1||a(e,t)}))})}function a(e,t){try{(n=i[e](t)).value instanceof F?Promise.resolve(n.value.v).then(c,h):u(o[0][2],n)}catch(e){u(o[0][3],e)}var n}function c(e){a("next",e)}function h(e){a("throw",e)}function u(e,t){e(t),o.shift(),o.length&&a(o[0][0],o[0][1])}},R=new DataView(new ArrayBuffer(0)),V=new Uint8Array(R.buffer),K=function(){try{R.getInt8(0)}catch(e){return e.constructor}throw new Error("never reached")}(),N=new K("Insufficient data"),H=new O,G=function(){function e(e,t,r,i,o,s,a,c){void 0===e&&(e=A.defaultCodec),void 0===t&&(t=void 0),void 0===r&&(r=n),void 0===i&&(i=n),void 0===o&&(o=n),void 0===s&&(s=n),void 0===a&&(a=n),void 0===c&&(c=H),this.extensionCodec=e,this.context=t,this.maxStrLength=r,this.maxBinLength=i,this.maxArrayLength=o,this.maxMapLength=s,this.maxExtLength=a,this.keyDecoder=c,this.totalPos=0,this.pos=0,this.view=R,this.bytes=V,this.headByte=-1,this.stack=[]}return e.prototype.reinitializeState=function(){this.totalPos=0,this.headByte=-1,this.stack.length=0},e.prototype.setBuffer=function(e){this.bytes=k(e),this.view=function(e){if(e instanceof ArrayBuffer)return new DataView(e);var t=k(e);return new DataView(t.buffer,t.byteOffset,t.byteLength)}(this.bytes),this.pos=0},e.prototype.appendBuffer=function(e){if(-1!==this.headByte||this.hasRemaining(1)){var t=this.bytes.subarray(this.pos),n=k(e),r=new Uint8Array(t.length+n.length);r.set(t),r.set(n,t.length),this.setBuffer(r)}else this.setBuffer(e)},e.prototype.hasRemaining=function(e){return this.view.byteLength-this.pos>=e},e.prototype.createExtraByteError=function(e){var t=this.view,n=this.pos;return new RangeError("Extra ".concat(t.byteLength-n," of ").concat(t.byteLength," byte(s) found at buffer[").concat(e,"]"))},e.prototype.decode=function(e){this.reinitializeState(),this.setBuffer(e);var t=this.doDecodeSync();if(this.hasRemaining(1))throw this.createExtraByteError(this.pos);return t},e.prototype.decodeMulti=function(e){return _(this,(function(t){switch(t.label){case 0:this.reinitializeState(),this.setBuffer(e),t.label=1;case 1:return this.hasRemaining(1)?[4,this.doDecodeSync()]:[3,3];case 2:return t.sent(),[3,1];case 3:return[2]}}))},e.prototype.decodeAsync=function(e){var t,n,r,i,o,s,a,c;return o=this,s=void 0,c=function(){var o,s,a,c,h,u,f,l;return _(this,(function(p){switch(p.label){case 0:o=!1,p.label=1;case 1:p.trys.push([1,6,7,12]),t=j(e),p.label=2;case 2:return[4,t.next()];case 3:if((n=p.sent()).done)return[3,5];if(a=n.value,o)throw this.createExtraByteError(this.totalPos);this.appendBuffer(a);try{s=this.doDecodeSync(),o=!0}catch(e){if(!(e instanceof K))throw e}this.totalPos+=this.pos,p.label=4;case 4:return[3,2];case 5:return[3,12];case 6:return c=p.sent(),r={error:c},[3,12];case 7:return p.trys.push([7,,10,11]),n&&!n.done&&(i=t.return)?[4,i.call(t)]:[3,9];case 8:p.sent(),p.label=9;case 9:return[3,11];case 10:if(r)throw r.error;return[7];case 11:return[7];case 12:if(o){if(this.hasRemaining(1))throw this.createExtraByteError(this.totalPos);return[2,s]}throw u=(h=this).headByte,f=h.pos,l=h.totalPos,new RangeError("Insufficient data in parsing ".concat(P(u)," at ").concat(l," (").concat(f," in the current buffer)"))}}))},new((a=void 0)||(a=Promise))((function(e,t){function n(e){try{i(c.next(e))}catch(e){t(e)}}function r(e){try{i(c.throw(e))}catch(e){t(e)}}function i(t){var i;t.done?e(t.value):(i=t.value,i instanceof a?i:new a((function(e){e(i)}))).then(n,r)}i((c=c.apply(o,s||[])).next())}))},e.prototype.decodeArrayStream=function(e){return this.decodeMultiAsync(e,!0)},e.prototype.decodeStream=function(e){return this.decodeMultiAsync(e,!1)},e.prototype.decodeMultiAsync=function(e,t){return W(this,arguments,(function(){var n,r,i,o,s,a,c,h,u;return _(this,(function(f){switch(f.label){case 0:n=t,r=-1,f.label=1;case 1:f.trys.push([1,13,14,19]),i=j(e),f.label=2;case 2:return[4,F(i.next())];case 3:if((o=f.sent()).done)return[3,12];if(s=o.value,t&&0===r)throw this.createExtraByteError(this.totalPos);this.appendBuffer(s),n&&(r=this.readArraySize(),n=!1,this.complete()),f.label=4;case 4:f.trys.push([4,9,,10]),f.label=5;case 5:return[4,F(this.doDecodeSync())];case 6:return[4,f.sent()];case 7:return f.sent(),0==--r?[3,8]:[3,5];case 8:return[3,10];case 9:if(!((a=f.sent())instanceof K))throw a;return[3,10];case 10:this.totalPos+=this.pos,f.label=11;case 11:return[3,2];case 12:return[3,19];case 13:return c=f.sent(),h={error:c},[3,19];case 14:return f.trys.push([14,,17,18]),o&&!o.done&&(u=i.return)?[4,F(u.call(i))]:[3,16];case 15:f.sent(),f.label=16;case 16:return[3,18];case 17:if(h)throw h.error;return[7];case 18:return[7];case 19:return[2]}}))}))},e.prototype.doDecodeSync=function(){e:for(;;){var e=this.readHeadByte(),t=void 0;if(e>=224)t=e-256;else if(e<192)if(e<128)t=e;else if(e<144){if(0!=(r=e-128)){this.pushMapState(r),this.complete();continue e}t={}}else if(e<160){if(0!=(r=e-144)){this.pushArrayState(r),this.complete();continue e}t=[]}else{var n=e-160;t=this.decodeUtf8String(n,0)}else if(192===e)t=null;else if(194===e)t=!1;else if(195===e)t=!0;else if(202===e)t=this.readF32();else if(203===e)t=this.readF64();else if(204===e)t=this.readU8();else if(205===e)t=this.readU16();else if(206===e)t=this.readU32();else if(207===e)t=this.readU64();else if(208===e)t=this.readI8();else if(209===e)t=this.readI16();else if(210===e)t=this.readI32();else if(211===e)t=this.readI64();else if(217===e)n=this.lookU8(),t=this.decodeUtf8String(n,1);else if(218===e)n=this.lookU16(),t=this.decodeUtf8String(n,2);else if(219===e)n=this.lookU32(),t=this.decodeUtf8String(n,4);else if(220===e){if(0!==(r=this.readU16())){this.pushArrayState(r),this.complete();continue e}t=[]}else if(221===e){if(0!==(r=this.readU32())){this.pushArrayState(r),this.complete();continue e}t=[]}else if(222===e){if(0!==(r=this.readU16())){this.pushMapState(r),this.complete();continue e}t={}}else if(223===e){if(0!==(r=this.readU32())){this.pushMapState(r),this.complete();continue e}t={}}else if(196===e){var r=this.lookU8();t=this.decodeBinary(r,1)}else if(197===e)r=this.lookU16(),t=this.decodeBinary(r,2);else if(198===e)r=this.lookU32(),t=this.decodeBinary(r,4);else if(212===e)t=this.decodeExtension(1,0);else if(213===e)t=this.decodeExtension(2,0);else if(214===e)t=this.decodeExtension(4,0);else if(215===e)t=this.decodeExtension(8,0);else if(216===e)t=this.decodeExtension(16,0);else if(199===e)r=this.lookU8(),t=this.decodeExtension(r,1);else if(200===e)r=this.lookU16(),t=this.decodeExtension(r,2);else{if(201!==e)throw new m("Unrecognized type byte: ".concat(P(e)));r=this.lookU32(),t=this.decodeExtension(r,4)}this.complete();for(var i=this.stack;i.length>0;){var o=i[i.length-1];if(0===o.type){if(o.array[o.position]=t,o.position++,o.position!==o.size)continue e;i.pop(),t=o.array}else{if(1===o.type){if(void 0,"string"!=(s=typeof t)&&"number"!==s)throw new m("The type of key must be string or number but "+typeof t);if("__proto__"===t)throw new m("The key __proto__ is not allowed");o.key=t,o.type=2;continue e}if(o.map[o.key]=t,o.readCount++,o.readCount!==o.size){o.key=null,o.type=1;continue e}i.pop(),t=o.map}}return t}var s},e.prototype.readHeadByte=function(){return-1===this.headByte&&(this.headByte=this.readU8()),this.headByte},e.prototype.complete=function(){this.headByte=-1},e.prototype.readArraySize=function(){var e=this.readHeadByte();switch(e){case 220:return this.readU16();case 221:return this.readU32();default:if(e<160)return e-144;throw new m("Unrecognized array type byte: ".concat(P(e)))}},e.prototype.pushMapState=function(e){if(e>this.maxMapLength)throw new m("Max length exceeded: map length (".concat(e,") > maxMapLengthLength (").concat(this.maxMapLength,")"));this.stack.push({type:1,size:e,key:null,readCount:0,map:{}})},e.prototype.pushArrayState=function(e){if(e>this.maxArrayLength)throw new m("Max length exceeded: array length (".concat(e,") > maxArrayLength (").concat(this.maxArrayLength,")"));this.stack.push({type:0,size:e,array:new Array(e),position:0})},e.prototype.decodeUtf8String=function(e,t){var n;if(e>this.maxStrLength)throw new m("Max length exceeded: UTF-8 byte length (".concat(e,") > maxStrLength (").concat(this.maxStrLength,")"));if(this.bytes.byteLength<this.pos+t+e)throw N;var r,i=this.pos+t;return r=this.stateIsMapKey()&&(null===(n=this.keyDecoder)||void 0===n?void 0:n.canBeCached(e))?this.keyDecoder.decode(this.bytes,i,e):e>g?function(e,t,n){var r=e.subarray(t,t+n);return v.decode(r)}(this.bytes,i,e):y(this.bytes,i,e),this.pos+=t+e,r},e.prototype.stateIsMapKey=function(){return this.stack.length>0&&1===this.stack[this.stack.length-1].type},e.prototype.decodeBinary=function(e,t){if(e>this.maxBinLength)throw new m("Max length exceeded: bin length (".concat(e,") > maxBinLength (").concat(this.maxBinLength,")"));if(!this.hasRemaining(e+t))throw N;var n=this.pos+t,r=this.bytes.subarray(n,n+e);return this.pos+=t+e,r},e.prototype.decodeExtension=function(e,t){if(e>this.maxExtLength)throw new m("Max length exceeded: ext length (".concat(e,") > maxExtLength (").concat(this.maxExtLength,")"));var n=this.view.getInt8(this.pos+t),r=this.decodeBinary(e,t+1);return this.extensionCodec.decode(r,n,this.context)},e.prototype.lookU8=function(){return this.view.getUint8(this.pos)},e.prototype.lookU16=function(){return this.view.getUint16(this.pos)},e.prototype.lookU32=function(){return this.view.getUint32(this.pos)},e.prototype.readU8=function(){var e=this.view.getUint8(this.pos);return this.pos++,e},e.prototype.readI8=function(){var e=this.view.getInt8(this.pos);return this.pos++,e},e.prototype.readU16=function(){var e=this.view.getUint16(this.pos);return this.pos+=2,e},e.prototype.readI16=function(){var e=this.view.getInt16(this.pos);return this.pos+=2,e},e.prototype.readU32=function(){var e=this.view.getUint32(this.pos);return this.pos+=4,e},e.prototype.readI32=function(){var e=this.view.getInt32(this.pos);return this.pos+=4,e},e.prototype.readU64=function(){var e,t,n=(e=this.view,t=this.pos,4294967296*e.getUint32(t)+e.getUint32(t+4));return this.pos+=8,n},e.prototype.readI64=function(){var e=i(this.view,this.pos);return this.pos+=8,e},e.prototype.readF32=function(){var e=this.view.getFloat32(this.pos);return this.pos+=4,e},e.prototype.readF64=function(){var e=this.view.getFloat64(this.pos);return this.pos+=8,e},e}(),X={};function q(e,t){return void 0===t&&(t=X),new G(t.extensionCodec,t.context,t.maxStrLength,t.maxBinLength,t.maxArrayLength,t.maxMapLength,t.maxExtLength).decode(e)}function J(e,t){return void 0===t&&(t=X),new G(t.extensionCodec,t.context,t.maxStrLength,t.maxBinLength,t.maxArrayLength,t.maxMapLength,t.maxExtLength).decodeMulti(e)}var Q=function(e,t){var n,r,i,o,s={label:0,sent:function(){if(1&i[0])throw i[1];return i[1]},trys:[],ops:[]};return o={next:a(0),throw:a(1),return:a(2)},"function"==typeof Symbol&&(o[Symbol.iterator]=function(){return this}),o;function a(o){return function(a){return function(o){if(n)throw new TypeError("Generator is already executing.");for(;s;)try{if(n=1,r&&(i=2&o[0]?r.return:o[0]?r.throw||((i=r.return)&&i.call(r),0):r.next)&&!(i=i.call(r,o[1])).done)return i;switch(r=0,i&&(o=[2&o[0],i.value]),o[0]){case 0:case 1:i=o;break;case 4:return s.label++,{value:o[1],done:!1};case 5:s.label++,r=o[1],o=[0];continue;case 7:o=s.ops.pop(),s.trys.pop();continue;default:if(!((i=(i=s.trys).length>0&&i[i.length-1])||6!==o[0]&&2!==o[0])){s=0;continue}if(3===o[0]&&(!i||o[1]>i[0]&&o[1]<i[3])){s.label=o[1];break}if(6===o[0]&&s.label<i[1]){s.label=i[1],i=o;break}if(i&&s.label<i[2]){s.label=i[2],s.ops.push(o);break}i[2]&&s.ops.pop(),s.trys.pop();continue}o=t.call(e,s)}catch(e){o=[6,e],r=0}finally{n=i=0}if(5&o[0])throw o[1];return{value:o[0]?o[1]:void 0,done:!0}}([o,a])}}},Y=function(e){return this instanceof Y?(this.v=e,this):new Y(e)},Z=function(e,t,n){if(!Symbol.asyncIterator)throw new TypeError("Symbol.asyncIterator is not defined.");var r,i=n.apply(e,t||[]),o=[];return r={},s("next"),s("throw"),s("return"),r[Symbol.asyncIterator]=function(){return this},r;function s(e){i[e]&&(r[e]=function(t){return new Promise((function(n,r){o.push([e,t,n,r])>1||a(e,t)}))})}function a(e,t){try{(n=i[e](t)).value instanceof Y?Promise.resolve(n.value.v).then(c,h):u(o[0][2],n)}catch(e){u(o[0][3],e)}var n}function c(e){a("next",e)}function h(e){a("throw",e)}function u(e,t){e(t),o.shift(),o.length&&a(o[0][0],o[0][1])}};function $(e){if(null==e)throw new Error("Assertion Failure: value must not be null nor undefined")}function ee(e){return null!=e[Symbol.asyncIterator]?e:function(e){return Z(this,arguments,(function(){var t,n,r,i;return Q(this,(function(o){switch(o.label){case 0:t=e.getReader(),o.label=1;case 1:o.trys.push([1,,9,10]),o.label=2;case 2:return[4,Y(t.read())];case 3:return n=o.sent(),r=n.done,i=n.value,r?[4,Y(void 0)]:[3,5];case 4:return[2,o.sent()];case 5:return $(i),[4,Y(i)];case 6:return[4,o.sent()];case 7:return o.sent(),[3,2];case 8:return[3,10];case 9:return t.releaseLock(),[7];case 10:return[2]}}))}))}(e)}function te(e,t){return void 0===t&&(t=X),n=this,r=void 0,o=function(){var n;return function(e,t){var n,r,i,o,s={label:0,sent:function(){if(1&i[0])throw i[1];return i[1]},trys:[],ops:[]};return o={next:a(0),throw:a(1),return:a(2)},"function"==typeof Symbol&&(o[Symbol.iterator]=function(){return this}),o;function a(o){return function(a){return function(o){if(n)throw new TypeError("Generator is already executing.");for(;s;)try{if(n=1,r&&(i=2&o[0]?r.return:o[0]?r.throw||((i=r.return)&&i.call(r),0):r.next)&&!(i=i.call(r,o[1])).done)return i;switch(r=0,i&&(o=[2&o[0],i.value]),o[0]){case 0:case 1:i=o;break;case 4:return s.label++,{value:o[1],done:!1};case 5:s.label++,r=o[1],o=[0];continue;case 7:o=s.ops.pop(),s.trys.pop();continue;default:if(!((i=(i=s.trys).length>0&&i[i.length-1])||6!==o[0]&&2!==o[0])){s=0;continue}if(3===o[0]&&(!i||o[1]>i[0]&&o[1]<i[3])){s.label=o[1];break}if(6===o[0]&&s.label<i[1]){s.label=i[1],i=o;break}if(i&&s.label<i[2]){s.label=i[2],s.ops.push(o);break}i[2]&&s.ops.pop(),s.trys.pop();continue}o=t.call(e,s)}catch(e){o=[6,e],r=0}finally{n=i=0}if(5&o[0])throw o[1];return{value:o[0]?o[1]:void 0,done:!0}}([o,a])}}}(this,(function(r){return n=ee(e),[2,new G(t.extensionCodec,t.context,t.maxStrLength,t.maxBinLength,t.maxArrayLength,t.maxMapLength,t.maxExtLength).decodeAsync(n)]}))},new((i=void 0)||(i=Promise))((function(e,t){function s(e){try{c(o.next(e))}catch(e){t(e)}}function a(e){try{c(o.throw(e))}catch(e){t(e)}}function c(t){var n;t.done?e(t.value):(n=t.value,n instanceof i?n:new i((function(e){e(n)}))).then(s,a)}c((o=o.apply(n,r||[])).next())}));var n,r,i,o}function ne(e,t){void 0===t&&(t=X);var n=ee(e);return new G(t.extensionCodec,t.context,t.maxStrLength,t.maxBinLength,t.maxArrayLength,t.maxMapLength,t.maxExtLength).decodeArrayStream(n)}function re(e,t){void 0===t&&(t=X);var n=ee(e);return new G(t.extensionCodec,t.context,t.maxStrLength,t.maxBinLength,t.maxArrayLength,t.maxMapLength,t.maxExtLength).decodeStream(n)}function ie(e,t){return void 0===t&&(t=X),re(e,t)}return t}()}));

            window.__HC_LAUNCHER_ENV__ = {
                APP_INTERFACE_PORT: ${appWebsocketPort},
                INSTALLED_APP_ID: "${appId}",
                APP_INTERFACE_TOKEN: ${token}
            };

            window.__HC_ZOME_CALL_SIGNER__ = {
                signZomeCall: async (request) => {
                    const nonce = Array.from(await crypto.getRandomValues(new Uint8Array(32)));
                    const expiresAt = 1e3*(Date.now()+3e5);
                    const payload = Array.from(MessagePack.encode(request.payload));

                    const zomeCallUnsigned = {
                        provenance: request.provenance,
                        cellIdDnaHash: request.cell_id[0],
                        cellIdAgentPubKey: request.cell_id[1],
                        zomeName: request.zome_name,
                        fnName: request.fn_name,
                        capSecret: null,
                        payload,
                        nonce,
                        expiresAt,
                    };
                    console.log('zomeCallUnsigned', zomeCallUnsigned);

                    const response = await window.__TAURI_INTERNALS__.invoke("plugin:holochain-foreground-service|sign_zome_call", zomeCallUnsigned);
                    console.log('response', response);

                    const zomeCallSigned = {
                        provenance: request.provenance,
                        cell_id: request.cell_id,
                        zome_name: request.zome_name,
                        fn_name: request.fn_name,
                        cap_secret: null,
                        payload,
                        nonce,
                        expires_at: expiresAt,
                        signature: Uint8Array.from(response.signature),
                    };
                    console.log('zomeCallSigned', zomeCallSigned);

                    return zomeCallSigned;
                }
            };
        """, null)
    }

    @Command
    fun signZomeCall(invoke: Invoke) {
        val args = invoke.parseArgs(SignZomeCallRequestArgs::class.java)
        val res = this.mService?.signZomeCall(SignZomeCallRequestAidl(
            args.provenance,
            args.cellIdDnaHash,
            args.cellIdAgentPubKey,
            args.zomeName,
            args.fnName,
            args.capSecret,
            args.payload,
            args.nonce,
            args.expiresAt,
        ))
        invoke.resolve(res!!.toJSObject())
        // Create app websocket
        /*
            TODO: return LauncherEnvironment
            export interface LauncherEnvironment {
                APP_INTERFACE_PORT?: number;
                ADMIN_INTERFACE_PORT?: number;
                INSTALLED_APP_ID?: InstalledAppId;
                APP_INTERFACE_TOKEN?: AppAuthenticationToken;
        } */        
    }

    /// Start service, which then starts the holochain conductor on initialization
    private fun launchInternal() {
        // Create notification channel
        val notificationManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(
            "HolochainServiceChannel",
            "Holochain Service",
            NotificationManager.IMPORTANCE_HIGH
        ))

        // Start service
        val intent = Intent(activity, HolochainService::class.java)
        activity.startForegroundService(intent)
        activity.bindService(intent, this.mConnection, 0)
    }
}

