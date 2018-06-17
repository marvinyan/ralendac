class CreateEvents < ActiveRecord::Migration[5.2]
  def change
    create_table :events do |t|
      t.string :description
      t.datetime :start_time
      t.datetime :end_time

      t.timestamps
    end
  end
end
