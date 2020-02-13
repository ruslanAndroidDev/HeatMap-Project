package ua.rdev.tripsmap

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.koin.androidx.viewmodel.ext.android.viewModel
import ua.rdev.tripsmap.model.SearchTripItem
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mapBoxMap: MapboxMap
    val viewModel: MainViewModel by viewModel()
    var dialog: ProgressDialog? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_key))
        setContentView(R.layout.activity_main)
        var mapView = findViewById<MapView>(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            mapBoxMap = it
            mapBoxMap.uiSettings.isCompassEnabled = false
            it.setStyle(
                Style.Builder().fromUri("asset://mapbox_style.json")
            ) { style ->
                viewModel.searches.observe(this, androidx.lifecycle.Observer {
                    addDataSource(style, it)
                    addHeatmapLayer(style)
                    addCircleLayer(style)

                })
                viewModel.onLoadData()
            }
        }

        viewModel.showProgress.observe(this, androidx.lifecycle.Observer {
            if (it) {
                dialog = ProgressDialog.show(
                    this, "",
                    "Оновлюємо дані...", true
                )
            } else {
                dialog?.hide()
            }
        })
    }


    val DATA_SOURCE_ID = "data"
    val HEATMAP_LAYER_ID = "data-heat"
    val HEATMAP_LAYER_SOURCE = "data"
    val CIRCLE_LAYER_ID = "data-circle"

    fun addDataSource(
        loadedMapStyle: Style,
        it: ArrayList<SearchTripItem>
    ) {
        var pointList = arrayListOf<Point>()
        for (i in it) {
            var point = Point.fromLngLat(i.startLocation.lng, i.startLocation.lat)
            pointList.add(point)
        }
        var feature = Feature.fromGeometry(MultiPoint.fromLngLats(pointList))
        var featureCollection = FeatureCollection.fromFeature(feature)
        var source = GeoJsonSource(DATA_SOURCE_ID, featureCollection)
        loadedMapStyle.addSource(source)
    }


    fun addHeatmapLayer(@NonNull loadedMapStyle: Style) {
        var layer = loadedMapStyle.getLayer(HEATMAP_LAYER_ID)
        if (layer == null) {
            layer = HeatmapLayer(HEATMAP_LAYER_ID, DATA_SOURCE_ID)
            layer.maxZoom = 15f
            layer.sourceLayer = HEATMAP_LAYER_SOURCE
            layer.setProperties(

// Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
// Begin color ramp at 0-stop with a 0-transparency color
// to create a blur-like effect.
                heatmapColor(
                    interpolate(
                        linear(), heatmapDensity(),
                        literal(0), rgba(33, 102, 172, 0),
                        literal(0.2), rgb(103, 169, 207),
                        literal(0.4), rgb(209, 229, 240),
                        literal(0.6), rgb(253, 219, 199),
                        literal(0.8), rgb(239, 138, 98),
                        literal(1), rgb(178, 24, 43)
                    )
                ),

// Increase the heatmap weight based on frequency and property magnitude

// Increase the heatmap color weight weight by zoom level
// heatmap-intensity is a multiplier on top of heatmap-weight
                heatmapIntensity(
                    interpolate(
                        linear(), zoom(),
                        stop(0, 1),
                        stop(15, 3)
                    )
                ),

// Adjust the heatmap radius by zoom level
                heatmapRadius(
                    interpolate(
                        linear(), zoom(),
                        stop(0, 2),
                        stop(15, 30)
                    )
                ),

// Transition from heatmap to circle layer by zoom level
                heatmapOpacity(
                    interpolate(
                        linear(), zoom(),
                        stop(7, 1),
                        stop(15, 0)
                    )
                )
            )

            loadedMapStyle.addLayerAbove(layer, "waterway-label")
        }
    }

    fun addCircleLayer(@NonNull loadedMapStyle: Style) {
        var circleLayer = loadedMapStyle.getLayer(CIRCLE_LAYER_ID)
        if (circleLayer == null) {

            circleLayer = CircleLayer(CIRCLE_LAYER_ID, DATA_SOURCE_ID)
            circleLayer.setProperties(

// Size circle radius by earthquake magnitude and zoom level
                circleRadius(
                    interpolate(
                        linear(), zoom(),
                        literal(7), interpolate(
                            linear(), get("mag"),
                            stop(1, 1),
                            stop(6, 4)
                        ),
                        literal(16), interpolate(
                            linear(), get("mag"),
                            stop(1, 5),
                            stop(6, 50)
                        )
                    )
                ),

// Color circle by earthquake magnitude
                circleColor(
                    interpolate(
                        linear(), get("mag"),
                        literal(1), rgba(33, 102, 172, 0),
                        literal(2), rgb(103, 169, 207),
                        literal(3), rgb(209, 229, 240),
                        literal(4), rgb(253, 219, 199),
                        literal(5), rgb(239, 138, 98),
                        literal(6), rgb(178, 24, 43)
                    )
                ),

// Transition from heatmap to circle layer by zoom level
                circleOpacity(
                    interpolate(
                        linear(), zoom(),
                        stop(7, 0),
                        stop(8, 1)
                    )
                ),
                circleStrokeColor("white"),
                circleStrokeWidth(1.0f)
            )

            loadedMapStyle.addLayerBelow(circleLayer, HEATMAP_LAYER_ID)
        }
    }
}
