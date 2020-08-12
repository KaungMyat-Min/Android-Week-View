package com.alamkanak.weekview.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewDisplayable
import com.alamkanak.weekview.sample.data.EventsDatabase
import com.alamkanak.weekview.sample.data.model.Event
import com.alamkanak.weekview.sample.util.observe
import com.alamkanak.weekview.sample.util.setupWithWeekView
import com.alamkanak.weekview.sample.util.showToast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle.LONG
import java.time.format.FormatStyle.SHORT
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.android.synthetic.main.activity_basic.weekView
import kotlinx.android.synthetic.main.view_toolbar.toolbar

private class ViewModel(
    private val database: EventsDatabase
) {
    val events = MutableLiveData<List<WeekViewDisplayable<Event>>>()

    fun fetchEvents(startDate: LocalDate, endDate: LocalDate) {
        val values = database.getEventsInRange(startDate.toCalendar(), endDate.toCalendar())
        events.value = values
    }
}

class BasicActivity : AppCompatActivity() {

    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    private val viewModel: ViewModel by lazy {
        ViewModel(EventsDatabase(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic)

        toolbar.setupWithWeekView(weekView)

        val adapter = BasicActivityWeekViewAdapter(loadMoreHandler = viewModel::fetchEvents)
        weekView.adapter = adapter

        weekView.setDateFormatter { date: LocalDate ->
            val weekdayLabel = weekdayFormatter.format(date)
            val dateLabel = dateFormatter.format(date)
            weekdayLabel + "\n" + dateLabel
        }

        viewModel.events.observe(this) { events ->
            adapter.submit(events)
        }
    }
}

private class BasicActivityWeekViewAdapter(
    private val loadMoreHandler: (startDate: LocalDate, endDate: LocalDate) -> Unit
) : WeekView.PagingAdapter<Event>() {

    private val formatter = DateTimeFormatter.ofLocalizedDateTime(LONG, SHORT)

    override fun onEventClick(data: Event) {
        context.showToast("Removed ${data.title}")
    }

    override fun onEmptyViewClick(time: LocalDateTime) {
        context.showToast("Empty view clicked at ${formatter.format(time)}")
    }

    override fun onEventLongClick(data: Event) {
        context.showToast("Long-clicked ${data.title}")
    }

    override fun onEmptyViewLongClick(time: LocalDateTime) {
        context.showToast("Empty view long-clicked at ${formatter.format(time)}")
    }

    override fun onLoadMore(startDate: LocalDate, endDate: LocalDate) {
        loadMoreHandler(startDate, endDate)
    }
}

internal fun LocalDate.toCalendar(): Calendar {
    val instant = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
    val calendar = Calendar.getInstance()
    calendar.time = Date.from(instant)
    return calendar
}
