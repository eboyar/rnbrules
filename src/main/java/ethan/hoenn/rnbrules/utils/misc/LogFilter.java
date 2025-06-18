package ethan.hoenn.rnbrules.utils.misc;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

public class LogFilter {

	public static void init() {
		Filter filter = new AbstractFilter() {
			@Override
			public Filter.Result filter(LogEvent event) {
				if (event.getMessage() != null && event.getMessage().getFormattedMessage().contains("Tried to add entity pixelmon:pixelmon but it was marked as removed already")) {
					return Filter.Result.DENY;
				}
				return Filter.Result.NEUTRAL;
			}

			@Override
			public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
				if (msg != null && msg.getFormattedMessage().contains("Tried to add entity pixelmon:pixelmon but it was marked as removed already")) {
					return Filter.Result.DENY;
				}
				return Filter.Result.NEUTRAL;
			}
		};

		((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(filter);
	}
}
