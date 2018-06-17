Rails.application.routes.draw do
  namespace :api, defaults: { format: :json } do
    resources :events, only: %i[index create show update destroy]
  end
end
