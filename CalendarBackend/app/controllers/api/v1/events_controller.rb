module Api::V1
  class EventsController < ApplicationController
    before_action :set_event, only: [:update, :destroy]

    def index
      events = Event.all
      render json: { events: events }
    end

    def create
      event = Event.create(event_params)

      if event.save
        render json: event
      else
        @status = 400
        @message = event.errors.full_messages
        render "/api/events/error", status: @status
      end
    end

    def update
      if @event.nil?
        @status = 404
        @message = ["Could not find event with id #{params[:id]}"]
        render "/api/events/error", status: @status
      elsif @event.update(event_params)
        render json: @event
      else
        @status = 400
        @message = @event.errors.full_messages
        render "/api/events/error", status: @status
      end
    end

    def destroy
      if @event.nil?
        @status = 404
        @message = ["Could not find event with id #{params[:id]}"]
        render "/api/events/error", status: @status
      else
        @event.destroy
        render json: @event
      end
    end

    private

    def event_params
      params.require(:event).permit(:description, :start_time, :end_time)
    end

    def set_event
      @event = Event.find_by(id: params[:id])
    end
  end
end