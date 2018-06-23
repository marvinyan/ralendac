Event.destroy_all
DatabaseCleaner.clean_with(:truncation)

date_range = Date.new(2017, 1, 1)..Date.new(2019, 12, 31)
zone = 'Eastern Time (US & Canada)'

date_range.each do |date|
    if rand() < 0.2
        descrip_len = rand(1..5)
        max_events = rand(1..6)

        Time.use_zone(zone) do
            max_events.times do
                start_of_day = Time.zone.parse(date.to_s)
                end_of_day = start_of_day.end_of_day

                random_time_1 = rand(start_of_day..end_of_day)
                random_time_2 = rand(start_of_day..end_of_day)

                start_time = [random_time_1, random_time_2].min.floor_to(15.minutes)
                end_time = [random_time_1, random_time_2].max.floor_to(15.minutes)

                description = Faker::Hipster.words(descrip_len, true).join(' ').capitalize
                Event.create({ description: description, start_time: start_time, end_time: end_time})
            end
        end
    end
end