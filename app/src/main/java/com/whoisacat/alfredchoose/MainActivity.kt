package com.whoisacat.alfredchoose

import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.whoisacat.alfredchoose.ui.theme.AlfredChooseTheme
import com.whoisacat.android.alfreddice.ShakeListener
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    companion object {
        private const val MILLS = 250L
        private const val NUMBER = "number"
        private const val QUANTITY_OF_STRINGS = 2
        private val STRINGS: Array<Int> = arrayOf(R.string.yes, R.string.no)
    }

    private var mNumber: Int? = null
    private var mShakeListener: ShakeListener? = null
    private var mVibrator: Vibrator? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val n = mNumber
        if (n != null) {
            outState.putInt(NUMBER, n)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mNumber = savedInstanceState.getInt(NUMBER, Int.MAX_VALUE)
        if (mNumber == Int.MAX_VALUE) mNumber = null
    }

    override fun onResume() {
        if (mNumber == null) setContent { InitScreen() }
        else setContent { ChooseScreen(getString(STRINGS[mNumber!!])) }
        if (mVibrator == null) {
            mVibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        super.onResume()
    }

    private fun getRandom(): Int {
        val n = Math.random().toFloat() * (QUANTITY_OF_STRINGS - 1)
        return n.roundToInt()
    }

    private fun callVibro() {
        if (mVibrator!!.hasVibrator()) {
            mVibrator!!.vibrate(MILLS)
        }
    }

    override fun onPause() {
        super.onPause()
        mVibrator = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mShakeListener = ShakeListener(applicationContext)
        val listener = object : ShakeListener.OnShakeListener {
            override fun onShake() {
                if (mVibrator != null) {
                    callVibro()
                }
                val random = getRandom()
                mNumber = random
                setContent {
                    AlfredChooseTheme {
                        ShowChoose(getString(STRINGS[random]))
                    }
                }

            }
        }
        mShakeListener!!.setOnShakeListener(listener)
    }
}

@Preview(showBackground = true)
@Composable
fun InitScreenTest() {
    InitScreen()
}

@Composable
private fun InitScreen() {
    AlfredChooseTheme {
        PlaceText("Не можешь решиться?")
    }
}

@Composable
private fun ChooseScreen(text: String) {
    AlfredChooseTheme {
        ShowChoose(text = text)
    }
}

@Composable
private fun PlaceText(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colors.primary,
            fontSize = 30.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Test() {
    ShowChoose("test value")
}

@Composable
fun ShowChoose(text: String) {
    Surface(color = MaterialTheme.colors.background) {

        PlaceText(text = text)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AlfredChooseTheme {
        ShowChoose("test value")
    }
}