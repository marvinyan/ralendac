class Api::EventsController < ApplicationController
  def index
    @events = Event.all
    render json: @events
  end

  def create
    @event = Event.create(event_params)

    if @event.save
      render json: @event
    else
      render json: @event.errors.full_messages, status: 400
    end
  end

  def update
    @event = Event.find_by(id: params[:id])

    if @event.nil?
      render json: ["Could not find event with id #{params[:id]}"], status: 404
    elsif @event.update(event_params)
      render json: @event
    else
      render json: @event.errors.full_messages, status: 400
    end
  end

  def destroy
    @event = Event.find_by(id: params[:id])

    if @event.nil?
      render json: ["Could not find event with id #{params[:id]}"].to_json, status: 404
    else
      @event.destroy
      render json: @event
    end
  end

  private

  def event_params
    params.require(:event).permit(:description, :start_time, :end_time)
  end
end
