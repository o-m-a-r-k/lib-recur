/*
 * Copyright (C) 2013 Marten Gajda <marten@dmfs.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.dmfs.rfc5545.recur;

import java.util.List;

import org.dmfs.rfc5545.recur.RecurrenceRule.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule.Part;


/**
 * Fast path for birthday type rules (i.e. instances that recur once a year on the same day and month).
 * 
 * @author Marten Gajda <marten@dmfs.org>
 */
public final class FastBirthdayIterator extends ByExpander
{
	/**
	 * The interval of the rule.
	 */
	private final int mInterval;

	/**
	 * A {@link LongArray} to hold the instances of the current interval.
	 */
	private final LongArray mResultSet = new LongArray(1);

	/**
	 * The next instance to iterate.
	 */
	private long mNextInstance;


	/**
	 * Create a new BirthdayTypeIterator for the given rule and start date.
	 * 
	 * @param rule
	 *            The rule to iterate.
	 * @param start
	 *            The first instance to iterate.
	 */
	public FastBirthdayIterator(RecurrenceRule rule, CalendarMetrics calendarMetrics, long firstInstance)
	{
		super(null, calendarMetrics, firstInstance);

		mInterval = rule.getInterval();

		mNextInstance = firstInstance;
	}


	/**
	 * Get an instance of a {@link FastBirthdayIterator}.
	 * 
	 * @param rule
	 *            The {@link RecurrenceRule} to iterate.
	 * @param calendarMetrics
	 *            The {@link CalendarMetrics} to use.
	 * @param start
	 *            The first instance.
	 * @return A {@link FastBirthdayIterator} instance or <code>null</code> if the rule is not siutable for this kind of optimization.
	 */
	public static FastBirthdayIterator getInstance(RecurrenceRule rule, CalendarMetrics calendarMetrics, long start)
	{
		if (rule.hasPart(Part.BYDAY) || rule.hasPart(Part.BYYEARDAY) || rule.hasPart(Part.BYWEEKNO) || rule.hasPart(Part.BYHOUR) || rule.hasPart(Part.BYMINUTE)
			|| rule.hasPart(Part.BYSECOND) || rule.hasPart(Part.BYSETPOS))
		{
			return null;
		}

		Freq freq = rule.getFreq();
		List<Integer> months = rule.getByPart(Part.BYMONTH);
		List<Integer> days = rule.getByPart(Part.BYMONTHDAY);

		if (months != null && months.size() == 1
			&& (days == null && (freq == Freq.MONTHLY || freq != Freq.YEARLY) || days != null && days.size() == 1 && days.get(0) > 0)
			|| (freq == Freq.YEARLY && months == null && days == null))
		{

			if (months != null)
			{
				start = Instance.setMonth(start, months.get(0) - 1);
			}

			if (days != null)
			{
				start = Instance.setDayOfMonth(start, days.get(0));
			}

			return new FastBirthdayIterator(rule, calendarMetrics, start);
		}
		return null;
	}


	@Override
	public long next()
	{
		long result = mNextInstance;
		mNextInstance = Instance.setYear(mNextInstance, Instance.year(mNextInstance) + mInterval);
		return result;
	}


	@Override
	LongArray nextSet()
	{
		mResultSet.clear();
		mResultSet.add(next());
		return mResultSet;
	}


	@Override
	void expand(long instance, long start)
	{
		// we don't need that.
	}

}