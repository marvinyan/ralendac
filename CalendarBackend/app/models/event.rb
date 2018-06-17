class Event < ApplicationRecord
    validates :description, :start_time, :end_time, presence: true
    validate :end_time_cannot_be_before_start_time
    validate :start_time_and_end_time_must_be_same_day

    def end_time_cannot_be_before_start_time
        if end_time < start_time
            errors.add(:end_time, "must be after start time")
        end
    end

    def start_time_and_end_time_must_be_same_day
        if start_time.beginning_of_day != end_time.beginning_of_day
            errors.add(:end_time, "must occur on the same day as the start time")
        end
    end
end
