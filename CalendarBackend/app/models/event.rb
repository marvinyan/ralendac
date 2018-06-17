class Event < ApplicationRecord
    validates :description, :start_time, :end_time, presence: true
    validate :end_time_cannot_be_before_start_time

    def end_time_cannot_be_before_start_time
        if end_time < start_time
            errors.add(:end_time, "must be after start time")
        end
    end
end
