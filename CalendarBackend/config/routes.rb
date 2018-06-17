Rails.application.routes.draw do
  namespace :api, defaults: { format: :json } do
    resources :events, only: %i[index create update destroy]
  end
end
