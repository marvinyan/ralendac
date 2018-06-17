class ChangeDateColumnsNullFalse < ActiveRecord::Migration[5.2]
  def change
    change_column_null(:events, :description, false)
    change_column_null(:events, :start_time, false)
    change_column_null(:events, :end_time, false)
  end
end
