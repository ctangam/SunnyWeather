package com.example.sunnyweather.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Slide
import com.example.sunnyweather.R
import com.example.sunnyweather.databinding.ActivityWeatherBinding
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

	private lateinit var binding: ActivityWeatherBinding

	val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val decorView = window.decorView
		decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		window.statusBarColor = Color.TRANSPARENT
		binding = ActivityWeatherBinding.inflate(layoutInflater)
		setContentView(binding.root)
		if (viewModel.locationLng.isEmpty()) {
			viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
		}
		if (viewModel.locationLat.isEmpty()) {
			viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
		}
		if (viewModel.placeName.isEmpty()) {
			viewModel.placeName = intent.getStringExtra("place_name") ?: ""
		}
		viewModel.weatherLiveData.observe(this) { result ->
			val weather = result.getOrNull()
			if (weather != null) {
				showWeatherInfo(weather)
			} else {
				Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
				result.exceptionOrNull()?.printStackTrace()
			}
			binding.swipeFresh.isRefreshing = false
		}
		binding.swipeFresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
		refreshWeather()
		binding.swipeFresh.setOnRefreshListener {
			refreshWeather()
		}

		binding.now.navBtn.setOnClickListener {
			binding.drawerLayout.openDrawer(GravityCompat.START)
		}
		binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
			override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
				TODO("Not yet implemented")
			}

			override fun onDrawerOpened(drawerView: View) {
				TODO("Not yet implemented")
			}

			override fun onDrawerClosed(drawerView: View) {
				val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
			}

			override fun onDrawerStateChanged(newState: Int) {
				TODO("Not yet implemented")
			}

		})
	}

	fun refreshWeather() {
		viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
		binding.swipeFresh.isRefreshing = true
	}

	private fun showWeatherInfo(weather: Weather) {
		val placeName = viewModel.placeName
		val realtime = weather.realtime
		val daily = weather.daily

		// now.xml
		val currentTempText = "${realtime.temperature.toInt()}℃"
		binding.now.currentTemp.text = currentTempText
		binding.now.currentSky.text = getSky(realtime.skycon).info
		val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
		binding.now.currentAQI.text = currentPM25Text
		binding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

		// forecast.xml
		binding.forecast.forecastLayout.removeAllViews()
		val days = daily.skycon.size
		for (i in 0 until days) {
			val skycon = daily.skycon[i]
			val temperature = daily.temperature[i]
			val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, binding.forecast.forecastLayout, false)
			val dateInfo: TextView = view.findViewById(R.id.dateInfo)
			val skyIcon: ImageView = view.findViewById(R.id.skyIcon)
			val skyInfo: TextView = view.findViewById(R.id.skyInfo)
			val temperatureInfo: TextView = view.findViewById(R.id.temperatureInfo)
			val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
			dateInfo.text = simpleDateFormat.format(skycon.date)
			val sky = getSky(skycon.value)
			skyIcon.setImageResource(sky.icon)
			skyInfo.text = sky.info
			val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
			temperatureInfo.text = tempText
			binding.forecast.forecastLayout.addView(view)
		}

		// life_index.xml
		val lifeIndex = daily.lifeIndex
		binding.life.coldRiskText.text = lifeIndex.coldRisk[0].desc
		binding.life.dressingText.text = lifeIndex.dressing[0].desc
		binding.life.ultravioletText.text = lifeIndex.ultraviolet[0].desc
		binding.life.carWashingText.text = lifeIndex.carWashing[0].desc

		binding.weatherLayout.visibility = View.VISIBLE
	}
}